package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzUserRecord;
import tech.sjiale.hoyo_achievement_server.mapper.ZzzUserRecordMapper;

import java.util.List;

@Slf4j
@Service("zzzUserRecordService")
@RequiredArgsConstructor
public class ZzzUserRecordServiceImpl extends ServiceImpl<ZzzUserRecordMapper, ZzzUserRecord> implements ZzzUserRecordService {

    private final ZzzAchievementService zzzAchievementService;
    private final ZzzBranchService zzzBranchService;

    /**
     * Get all ZZZ achievements records by account uuid
     *
     * @param uuid Account uuid
     * @return List of ZZZ achievements records
     */
    public ServiceResponse<List<ZzzUserRecord>> getAllRecordByUUID(String uuid) {
        List<ZzzUserRecord> list = this.lambdaQuery().eq(ZzzUserRecord::getAccountUuid, uuid).list();
        if (list == null) {
            return ServiceResponse.error("Failed to get ZZZ achievements records for uuid: " + uuid);
        }
        return ServiceResponse.success("Get all ZZZ achievements records by uuid successfully: " + uuid, list);
    }

    /**
     * Update ZZZ achievement record by achievement id and account uuid
     *
     * @param uuid           Account uuid
     * @param achievementId  ZZZ achievement id
     * @param completeStatus Complete status
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateRecordById(String uuid, Integer achievementId, Integer completeStatus) {
        // Check if achievement exists
        if (!zzzAchievementService.getAchievementById(achievementId).success()) {
            return ServiceResponse.error("ZZZ Achievement id doesn't exist: " + achievementId);
        }

        // Update current achievement record
        updateRecord(uuid, achievementId, completeStatus);

        // Update achievements in same branch
        ServiceResponse<List<Integer>> response = zzzBranchService.getAchievementInSameBranch(achievementId);
        if (!response.success()) {
            throw new RuntimeException("Failed to get ZZZ achievements in same branch.");
        }
        if (!response.data().isEmpty()) {
            Integer branchStatus = completeStatus == 1 ? 2 : 0;
            for (Integer achievement : response.data()) {
                updateRecord(uuid, achievement, branchStatus);
            }
        }
        return ServiceResponse.success("Update ZZZ achievement record successfully.");
    }

    /**
     * Update ZZZ achievement record by achievement id and account uuid; if the record doesn't exist, insert a new one
     *
     * @param uuid           Account uuid
     * @param achievementId  ZZZ achievement id
     * @param completeStatus Complete status
     */
    private void updateRecord(String uuid, Integer achievementId, Integer completeStatus) {
        ZzzUserRecord record = this.lambdaQuery()
                .eq(ZzzUserRecord::getAccountUuid, uuid)
                .eq(ZzzUserRecord::getAchievementId, achievementId)
                .one();
        if (record != null) {
            this.lambdaUpdate()
                    .eq(ZzzUserRecord::getAccountUuid, uuid)
                    .eq(ZzzUserRecord::getAchievementId, achievementId)
                    .set(ZzzUserRecord::getComplete, completeStatus)
                    .update();
        } else {
            ZzzUserRecord newRecord = new ZzzUserRecord();
            newRecord.setAccountUuid(uuid);
            newRecord.setAchievementId(achievementId);
            newRecord.setComplete(completeStatus);
            this.save(newRecord);
        }
    }
}

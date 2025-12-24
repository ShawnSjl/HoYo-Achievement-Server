package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.SrUserRecord;
import tech.sjiale.hoyo_achievement_server.mapper.SrUserRecordMapper;

import java.util.List;

@Slf4j
@Service("srUserRecordService")
@RequiredArgsConstructor
public class SrUserRecordServiceImpl extends ServiceImpl<SrUserRecordMapper, SrUserRecord> implements SrUserRecordService {

    private final SrAchievementService srAchievementService;
    private final SrBranchService srBranchService;

    /**
     * Get all SR achievements records by account uuid
     *
     * @param uuid Account uuid
     * @return List of SR achievements records
     */
    public ServiceResponse<List<SrUserRecord>> getAllRecordByUUID(String uuid) {
        List<SrUserRecord> list = this.lambdaQuery().eq(SrUserRecord::getAccountUuid, uuid).list();
        if (list == null) {
            return ServiceResponse.error("Failed to get SR achievements records for uuid: " + uuid);
        }
        return ServiceResponse.success("Get all SR achievements records by uuid successfully: " + uuid, list);
    }

    /**
     * Update SR achievement record by achievement id and account uuid
     *
     * @param uuid           Account uuid
     * @param achievementId  SR achievement id
     * @param completeStatus Complete status
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateRecordById(String uuid, Integer achievementId, Integer completeStatus) {
        // Check if achievement exists
        if (!srAchievementService.getAchievementById(achievementId).success()) {
            return ServiceResponse.error("SR Achievement id doesn't exist: " + achievementId);
        }

        // Update current achievement record
        updateRecord(uuid, achievementId, completeStatus);

        // Update achievements in same branch
        ServiceResponse<List<Integer>> response = srBranchService.getAchievementInSameBranch(achievementId);
        if (!response.success()) {
            throw new RuntimeException("Failed to get SR achievements in same branch.");
        }
        if (!response.data().isEmpty()) {
            Integer branchStatus = completeStatus == 1 ? 2 : 0;
            for (Integer achievement : response.data()) {
                updateRecord(uuid, achievement, branchStatus);
            }
        }
        return ServiceResponse.success("Update SR achievement record successfully.");
    }

    /**
     * Update SR achievement record by achievement id and account uuid; if the record doesn't exist, insert a new one
     *
     * @param uuid           Account uuid
     * @param achievementId  SR achievement id
     * @param completeStatus Complete status
     */
    private void updateRecord(String uuid, Integer achievementId, Integer completeStatus) {
        SrUserRecord record = this.lambdaQuery()
                .eq(SrUserRecord::getAccountUuid, uuid)
                .eq(SrUserRecord::getAchievementId, achievementId)
                .one();
        if (record != null) {
            this.lambdaUpdate()
                    .eq(SrUserRecord::getAccountUuid, uuid)
                    .eq(SrUserRecord::getAchievementId, achievementId)
                    .set(SrUserRecord::getComplete, completeStatus)
                    .update();
        } else {
            SrUserRecord newRecord = new SrUserRecord();
            newRecord.setAccountUuid(uuid);
            newRecord.setAchievementId(achievementId);
            newRecord.setComplete(completeStatus);
            this.save(newRecord);
        }
    }
}

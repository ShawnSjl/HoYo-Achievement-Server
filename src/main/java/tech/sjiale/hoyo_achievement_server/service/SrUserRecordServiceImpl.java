package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.SrAchievementRecordDto;
import tech.sjiale.hoyo_achievement_server.entity.SrUserRecord;
import tech.sjiale.hoyo_achievement_server.mapper.SrUserRecordMapper;

import java.util.List;

@Slf4j
@Service("srUserRecordService")
@RequiredArgsConstructor
public class SrUserRecordServiceImpl extends ServiceImpl<SrUserRecordMapper, SrUserRecord> implements SrUserRecordService {

    private final SrAchievementService srAchievementService;
    private final SrBranchService srBranchService;
    private final AccountService accountService;

    /**
     * Get all SR achievements with empty records
     */
    public ServiceResponse<List<SrAchievementRecordDto>> getAllAchievementsEmptyRecord() {
        List<SrAchievementRecordDto> list = this.baseMapper.selectAllAchievementsWithEmptyRecord();
        log.debug("Get all SR achievements with empty records.");
        return ServiceResponse.success("Get all SR achievements with empty records successfully.", list);
    }

    /**
     * Get all SR achievements records by account uuid
     */
    public ServiceResponse<List<SrAchievementRecordDto>> getAllAchievementsRecordByUUID(String uuid) {
        List<SrAchievementRecordDto> list = this.baseMapper.selectAllAchievementsRecordByUUID(uuid);
        log.debug("Get all SR achievements records by uuid: {}", uuid);
        return ServiceResponse.success("Get all SR achievements records by uuid successfully.", list);
    }

    /**
     * Update SR achievement record by achievement id and account uuid
     */
    @Transactional
    public ServiceResponse<Boolean> updateRecordById(String uuid, Integer achievementId, Integer completeStatus) {
        // Check if account exists
        if (!accountService.getAccountByUuid(uuid).success()) {
            log.error("Account uuid doesn't exist: {}", uuid);
            throw new IllegalArgumentException("Account uuid doesn't exist.");
        }

        // Check if achievement exists
        if (!srAchievementService.getAchievementById(achievementId).success()) {
            log.error("Achievement id doesn't exist: {}", achievementId);
            throw new IllegalArgumentException("SR Achievement id doesn't exist.");
        }

        // Update current achievement record
        updateRecord(uuid, achievementId, completeStatus);

        // Update achievements in same branch
        ServiceResponse<List<Integer>> response = srBranchService.getAchievementInSameBranch(achievementId);
        if (response.success() && !response.data().isEmpty()) {
            Integer branchStatus = completeStatus == 1 ? 2 : 0;
            for (Integer achievement : response.data()) {
                updateRecord(uuid, achievement, branchStatus);
            }
        } else {
            throw new RuntimeException("Failed to get SR achievements in same branch.");
        }

        log.debug("Update SR achievement record successfully.");
        return ServiceResponse.success("Update SR achievement record successfully.", true);
    }

    /**
     * Update SR achievement record by achievement id and account uuid; if record doesn't exist, insert a new one
     */
    private void updateRecord(String uuid, Integer achievementId, Integer completeStatus) {
        SrUserRecord record = this.lambdaQuery()
                .eq(SrUserRecord::getAccount_uuid, uuid)
                .eq(SrUserRecord::getAchievement_id, achievementId)
                .one();
        if (record != null) {
            this.lambdaUpdate()
                    .eq(SrUserRecord::getAccount_uuid, uuid)
                    .eq(SrUserRecord::getAchievement_id, achievementId)
                    .set(SrUserRecord::getComplete, completeStatus)
                    .update();
        } else {
            SrUserRecord newRecord = new SrUserRecord();
            newRecord.setAccount_uuid(uuid);
            newRecord.setAchievement_id(achievementId);
            newRecord.setComplete(completeStatus);
            this.save(newRecord);
        }
    }
}

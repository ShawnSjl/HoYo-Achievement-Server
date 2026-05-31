package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ChangeLog;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Achievement;
import tech.sjiale.hoyo_achievement_server.entity.UserRecord;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;
import tech.sjiale.hoyo_achievement_server.mapper.UserRecordMapper;

import java.util.List;

@Slf4j
@Service("userRecordService")
@RequiredArgsConstructor
public class UserRecordServiceImpl extends ServiceImpl<UserRecordMapper, UserRecord> implements UserRecordService {

    private final AchievementService achievementService;
    private final SseServiceImpl sseService;

    /**
     * Get user records by account uuid
     *
     * @param uuid Account uuid
     * @return List of user records
     */
    @Override
    public ServiceResponse<List<UserRecord>> getAllRecordByUUID(String uuid) {
        List<UserRecord> list = this.lambdaQuery()
                .eq(UserRecord::getAccountUuid, uuid)
                .list();
        if (list == null) {
            return ServiceResponse.error("Failed to get user records for uuid: " + uuid);
        }
        return ServiceResponse.success("Get all user records by uuid successfully: " + uuid, list);
    }

    @Override
    @Transactional
    public ServiceResponse<?> updateRecordById(Long userId, String clientId, String uuid, GameId gameId, Integer achievementId, Integer completeStatus) {
        // Check if achievement exists
        if (!achievementService.getAchievementById(gameId, achievementId).success()) {
            return ServiceResponse.error("Achievement id doesn't exist: " + achievementId);
        }

        // Update current achievement record
        updateRecord(uuid, gameId, achievementId, completeStatus);

        // Update achievements in same branch
        ServiceResponse<List<Achievement>> response = achievementService.getAchievementsInSameBranch(gameId, achievementId);
        if (!response.success()) {
            throw new RuntimeException("Failed to get achievements in same branch.");
        }
        if (!response.data().isEmpty()) {
            Integer branchStatus = completeStatus == 1 ? 2 : 0;
            for (Achievement achievement : response.data()) {
                updateRecord(uuid, achievement.getGameId(), achievement.getAchievementId(), branchStatus);
            }
        }

        // Create changelog and broadcast it
        ChangeLog changeLog = new ChangeLog();
        changeLog.setEntityType(ChangeEntityType.ACCOUNT_RECORD);
        // use 'uuid'&'game_id'&'achievement_id' as key
        changeLog.setEntityId(uuid + "&" + gameId + "&" + achievementId);
        changeLog.setAction(ChangeAction.UPDATE);
        sseService.broadcastUpdate(userId, clientId, changeLog);

        return ServiceResponse.success("Update achievement record successfully.");
    }

    /**
     * Update user records in a batch
     *
     * @param userId
     * @param clientId
     * @param userRecords List of user records
     * @return ServiceResponse
     */
    @Override
    public ServiceResponse<?> updateRecordBatch(Long userId, String clientId, List<UserRecord> userRecords) {
        for (UserRecord userRecord : userRecords) {
            // Check if achievement exists
            if (!achievementService.getAchievementById(userRecord.getGameId(), userRecord.getAchievementId()).success()) {
                return ServiceResponse.error("Achievement id doesn't exist: " + userRecord.getAchievementId());
            }

            updateRecord(userRecord.getAccountUuid(), userRecord.getGameId(), userRecord.getAchievementId(), userRecord.getComplete());

            // Update achievements in same branch
            ServiceResponse<List<Achievement>> response = achievementService.getAchievementsInSameBranch(userRecord.getGameId(), userRecord.getAchievementId());
            if (!response.success()) {
                throw new RuntimeException("Failed to get achievements in same branch.");
            }
            if (!response.data().isEmpty()) {
                Integer branchStatus = userRecord.getComplete() == 1 ? 2 : 0;
                for (Achievement achievement : response.data()) {
                    updateRecord(userRecord.getAccountUuid(), achievement.getGameId(), achievement.getAchievementId(), branchStatus);
                }
            }
        }

        // Create changelog and broadcast it
        ChangeLog changeLog = new ChangeLog();
        changeLog.setEntityType(ChangeEntityType.ACCOUNT_RECORD);
        // use 'uuid' as entity key, means need to update all records of the account
        changeLog.setEntityId(userRecords.getFirst().getAccountUuid());
        changeLog.setAction(ChangeAction.UPDATE);
        sseService.broadcastUpdate(userId, clientId, changeLog);

        return ServiceResponse.success("Update achievement record batch successfully.");
    }

    private void updateRecord(String uuid, GameId gameId, Integer achievementId, Integer completeStatus) {
        UserRecord record = this.lambdaQuery()
                .eq(UserRecord::getAccountUuid, uuid)
                .eq(UserRecord::getGameId, gameId)
                .eq(UserRecord::getAchievementId, achievementId)
                .one();
        if (record != null) {
            this.lambdaUpdate()
                    .eq(UserRecord::getAccountUuid, uuid)
                    .eq(UserRecord::getGameId, gameId)
                    .eq(UserRecord::getAchievementId, achievementId)
                    .set(UserRecord::getComplete, completeStatus)
                    .update();
        } else {
            UserRecord newRecord = new UserRecord();
            newRecord.setAccountUuid(uuid);
            newRecord.setGameId(gameId);
            newRecord.setAchievementId(achievementId);
            newRecord.setComplete(completeStatus);
            this.save(newRecord);
        }
    }
}

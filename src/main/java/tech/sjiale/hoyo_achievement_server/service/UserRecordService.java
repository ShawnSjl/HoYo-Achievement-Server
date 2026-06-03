package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.UserRecord;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

import java.util.List;

public interface UserRecordService extends IService<UserRecord> {
    ServiceResponse<List<UserRecord>> getAllRecordByUUID(String uuid);

    ServiceResponse<?> updateRecordById(Long userId, String clientId, String uuid, GameId gameId, Integer achievementId, Integer completeStatus);

    ServiceResponse<?> updateRecordBatch(Long userId, String clientId, List<UserRecord> userRecords);
}

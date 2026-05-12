package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.GameInfo;

import java.util.List;
import java.util.Map;

public interface GameInfoService extends IService<GameInfo> {
    ServiceResponse<List<GameInfo>> getAllGameInfo();

    ServiceResponse<GameInfo> getGameInfoByGameId(String gameId);

    // Data migration use
    ServiceResponse<?> insertGameInfoBatch(List<Map<String, Object>> gameInfoMapList);

    ServiceResponse<?> updateGameInfoBatch(List<Map<String, Object>> gameInfoMapList);

    ServiceResponse<?> deleteGameInfoBatch(List<Map<String, Object>> gameInfoMapList);
}

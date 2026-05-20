package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Achievement;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

import java.util.List;
import java.util.Map;

public interface AchievementService extends IService<Achievement> {

    ServiceResponse<List<Achievement>> getAllAchievementsByGameId(GameId gameId);

//    ServiceResponse<List<Achievement>> getAllBranchesByGameId(GameId gameId);

    ServiceResponse<Achievement> getAchievementById(GameId gameId, Integer achievementId);

    ServiceResponse<List<Achievement>> getAchievementsInSameBranch(GameId gameId, Integer achievementId);

    // Data migration use
    ServiceResponse<?> insertAchievementBatch(List<Map<String, Object>> achievementMapList);

    ServiceResponse<?> updateAchievementBatch(List<Map<String, Object>> achievementMapList);

    ServiceResponse<?> deleteAchievementBatch(List<Map<String, Object>> achievementMapList);
}

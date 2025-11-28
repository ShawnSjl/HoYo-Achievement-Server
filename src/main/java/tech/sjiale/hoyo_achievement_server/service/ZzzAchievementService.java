package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzAchievement;

import java.util.List;
import java.util.Map;

public interface ZzzAchievementService extends IService<ZzzAchievement> {
    ServiceResponse<ZzzAchievement> getAchievementById(Integer achievementId);

    // Import use
    ServiceResponse<?> insertAchievements(List<Map<String, Object>> achievementMapList);
}

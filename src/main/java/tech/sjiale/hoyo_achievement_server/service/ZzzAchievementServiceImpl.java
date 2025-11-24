package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzAchievement;
import tech.sjiale.hoyo_achievement_server.mapper.ZzzAchievementMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("zzzAchievementService")
public class ZzzAchievementServiceImpl extends ServiceImpl<ZzzAchievementMapper, ZzzAchievement> implements ZzzAchievementService {

    /**
     * Get all ZZZ achievements
     */
    public ServiceResponse<List<ZzzAchievement>> getAllAchievements() {
        List<ZzzAchievement> achievements = this.list();

        if (achievements == null || achievements.isEmpty()) {
            log.error("No ZZZ achievements found.");
            return ServiceResponse.error("No ZZZ achievements found.");
        }

        log.debug("Get all ZZZ achievements successfully.");
        return ServiceResponse.success("Get all ZZZ achievements successfully.", achievements);
    }

    /**
     * Insert ZZZ achievements; should only be called by migration service
     */
    @Transactional
    public ServiceResponse<?> insertAchievements(List<Map<String, Object>> achievementMapList) {
        for (Map<String, Object> achievementMap : achievementMapList) {
            Integer achievementId = (Integer) achievementMap.get("achievement_id");
            Integer classId = (Integer) achievementMap.get("class_id");
            String name = achievementMap.get("name").toString();
            String description = achievementMap.get("description").toString();
            Integer rewardLevel = (Integer) achievementMap.get("reward_level");
            Integer hidden = (Integer) achievementMap.get("hidden");
            String gameVersion = achievementMap.get("game_version").toString();

            if (achievementId == null || classId == null || name == null || description == null || rewardLevel == null || hidden == null || gameVersion == null) {
                log.error("Invalid achievement data: achievement_id={}, class={}, name={}, description={}, " +
                        "reward_level={}, hidden={}, game_version={}", achievementId, classId, name, description, rewardLevel, hidden, gameVersion);
                throw new IllegalArgumentException("Invalid achievement data.");
            }

            ZzzAchievement achievement = new ZzzAchievement();
            achievement.setAchievement_id(achievementId);
            achievement.setClass_id(classId);
            achievement.setName(name);
            achievement.setDescription(description);
            achievement.setReward_level(rewardLevel);
            achievement.setHidden(hidden);
            achievement.setGame_version(gameVersion);
            this.save(achievement);
        }
        log.debug("Insert ZZZ achievements successfully.");
        return ServiceResponse.success("Insert ZZZ achievements successfully.");
    }
}

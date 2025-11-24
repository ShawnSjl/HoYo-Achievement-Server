package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.SrAchievement;
import tech.sjiale.hoyo_achievement_server.mapper.SrAchievementMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("srAchievementService")
public class SrAchievementServiceImpl extends ServiceImpl<SrAchievementMapper, SrAchievement> implements SrAchievementService {

    /**
     * Get all SR achievements
     */
    public ServiceResponse<List<SrAchievement>> getAllAchievements() {
        List<SrAchievement> achievements = this.list();

        if (achievements == null || achievements.isEmpty()) {
            log.error("No SR achievements found.");
            return ServiceResponse.error("No SR achievements found.");
        }

        log.debug("Get all SR achievements successfully.");
        return ServiceResponse.success("Get all SR achievements successfully.", achievements);
    }

    /**
     * Insert SR achievements; should only be called by migration service
     */
    @Transactional
    public ServiceResponse<?> insertAchievements(List<Map<String, Object>> achievementMapList) {
        for (Map<String, Object> achievementMap : achievementMapList) {
            Integer achievementId = (Integer) achievementMap.get("achievement_id");
            String className = achievementMap.get("class").toString();
            String name = achievementMap.get("name").toString();
            String description = achievementMap.get("description").toString();
            Integer rewardLevel = (Integer) achievementMap.get("reward_level");
            Integer hidden = (Integer) achievementMap.get("hidden");
            String gameVersion = achievementMap.get("game_version").toString();

            if (achievementId == null || className == null || name == null || description == null || rewardLevel == null || hidden == null || gameVersion == null) {
                log.error("Invalid achievement data: achievement_id={}, class={}, name={}, description={}, reward_level={}, hidden={}, game_version={}", achievementId, className, name, description, rewardLevel, hidden, gameVersion);
                throw new IllegalArgumentException("Invalid achievement data.");
            }

            SrAchievement achievement = new SrAchievement();
            achievement.setAchievement_id(achievementId);
            achievement.setClassName(className);
            achievement.setName(name);
            achievement.setDescription(description);
            achievement.setReward_level(rewardLevel);
            achievement.setHidden(hidden);
            achievement.setGame_version(gameVersion);
            this.save(achievement);
        }
        log.debug("Insert SR achievements successfully.");
        return ServiceResponse.success("Insert SR achievements successfully.");
    }
}

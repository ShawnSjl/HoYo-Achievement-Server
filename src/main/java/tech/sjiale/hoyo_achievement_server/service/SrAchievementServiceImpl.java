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
     * Get SR achievement by id
     *
     * @param achievementId achievement id
     * @return ServiceResponse with SrAchievement
     */
    public ServiceResponse<SrAchievement> getAchievementById(Integer achievementId) {
        // Get achievement by id
        SrAchievement achievement = this.lambdaQuery().eq(SrAchievement::getAchievementId, achievementId).one();
        if (achievement == null) {
            log.error("No SR achievement found with id: {}", achievementId);
            return ServiceResponse.error("No achievement found with id: " + achievementId);
        }

        log.debug("Get SR achievement by id successfully.");
        return ServiceResponse.success("Get SR achievement by id successfully.", achievement);
    }

    /**
     * Insert SR achievements; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertAchievements(List<Map<String, Object>> achievementMapList) {
        for (Map<String, Object> achievementMap : achievementMapList) {
            Integer achievementId = (Integer) achievementMap.get("achievement_id");
            String className = achievementMap.get("class_name").toString();
            String name = achievementMap.get("name").toString();
            String description = achievementMap.get("description").toString();
            Integer rewardLevel = (Integer) achievementMap.get("reward_level");
            String gameVersion = achievementMap.get("game_version").toString();

            if (achievementId == null || className == null || name == null || description == null || rewardLevel == null || gameVersion == null) {
                log.error("Invalid achievement data: achievement_id={}, class={}, name={}, description={}, reward_level={}, game_version={}", achievementId, className, name, description, rewardLevel, gameVersion);
                throw new IllegalArgumentException("Invalid SR achievement data.");
            }

            SrAchievement achievement = new SrAchievement();
            achievement.setAchievementId(achievementId);
            achievement.setClassName(className);
            achievement.setName(name);
            achievement.setDescription(description);
            achievement.setRewardLevel(rewardLevel);
            achievement.setGameVersion(gameVersion);
            this.save(achievement);
        }
        log.debug("Insert SR achievements successfully.");
        return ServiceResponse.success("Insert SR achievements successfully.");
    }
}

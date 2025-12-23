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
     * Get ZZZ achievement by id
     *
     * @param achievementId achievement id
     * @return ServiceResponse with ZzzAchievement
     */
    public ServiceResponse<ZzzAchievement> getAchievementById(Integer achievementId) {
        // Get achievement by id
        ZzzAchievement achievement = this.lambdaQuery().eq(ZzzAchievement::getAchievementId, achievementId).one();
        if (achievement == null) {
            log.error("No ZZZ achievement found with id: {}", achievementId);
            return ServiceResponse.error("No ZZZ achievement found with id: " + achievementId);
        }

        log.debug("Get SR achievement by id successfully.");
        return ServiceResponse.success("Get SR achievement by id successfully.", achievement);
    }

    /**
     * Get all ZZZ achievements
     *
     * @return ServiceResponse with a list of ZzzAchievement
     */
    public ServiceResponse<List<ZzzAchievement>> getAllAchievements() {
        return ServiceResponse.success("Get all ZZZ achievements successfully.", this.list());
    }

    /**
     * Insert ZZZ achievements; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertAchievements(List<Map<String, Object>> achievementMapList) {
        for (Map<String, Object> achievementMap : achievementMapList) {
            Integer achievementId = (Integer) achievementMap.get("achievement_id");
            Integer classId = (Integer) achievementMap.get("class_id");
            String name = achievementMap.get("name").toString();
            String description = achievementMap.get("description").toString();
            Integer rewardLevel = (Integer) achievementMap.get("reward_level");
            String gameVersion = achievementMap.get("game_version").toString();

            if (achievementId == null || classId == null || name == null || description == null || rewardLevel == null || gameVersion == null) {
                log.error("Invalid achievement data: achievement_id={}, class={}, name={}, description={}, " +
                        "reward_level={}, game_version={}", achievementId, classId, name, description, rewardLevel, gameVersion);
                throw new IllegalArgumentException("Invalid ZZZ achievement data.");
            }

            ZzzAchievement achievement = new ZzzAchievement();
            achievement.setAchievementId(achievementId);
            achievement.setClassId(classId);
            achievement.setName(name);
            achievement.setDescription(description);
            achievement.setRewardLevel(rewardLevel);
            achievement.setGameVersion(gameVersion);
            this.save(achievement);
        }
        log.debug("Insert ZZZ achievements successfully.");
        return ServiceResponse.success("Insert ZZZ achievements successfully.");
    }
}

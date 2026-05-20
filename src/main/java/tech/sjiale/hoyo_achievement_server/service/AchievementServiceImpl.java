package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Achievement;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;
import tech.sjiale.hoyo_achievement_server.mapper.AchievementMapper;
import tech.sjiale.hoyo_achievement_server.util.MigrationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("achievementService")
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {

    /**
     * Get all achievements by game id
     *
     * @param gameId game ID
     * @return ServiceResponse with a list of Achievement
     */
    @Override
    public ServiceResponse<List<Achievement>> getAllAchievementsByGameId(GameId gameId) {
        // Get achievements by game id
        List<Achievement> achievements = this.lambdaQuery()
                .eq(Achievement::getGameId, gameId)
                .list();
        if (achievements == null || achievements.isEmpty()) {
            return ServiceResponse.error("No achievements found with game id: " + gameId);
        }

        log.debug("Get achievements by game id: {} successfully.", gameId);
        return ServiceResponse.success("Get achievements by game id: " + gameId, achievements);
    }

//    @Override
//    public ServiceResponse<List<Achievement>> getAllBranchesByGameId(GameId gameId) {
//        return null;
//    }

    /**
     * Get achievement by game id and achievement id
     *
     * @param gameId        game ID
     * @param achievementId achievement ID
     * @return ServiceResponse with Achievement
     */
    @Override
    public ServiceResponse<Achievement> getAchievementById(GameId gameId, Integer achievementId) {
        // Get achievement by game id and achievement id
        Achievement achievement = this.lambdaQuery()
                .eq(Achievement::getGameId, gameId)
                .eq(Achievement::getAchievementId, achievementId)
                .one();
        if (achievement == null) {
            return ServiceResponse.error("No achievement found with game id: " + gameId + ", achievement id: " + achievementId);
        }

        log.debug("Get achievement by game id: {} and achievement id: {} successfully.", gameId, achievementId);
        return ServiceResponse.success("Get achievement by game id: " + gameId + " and achievement id: " + achievementId, achievement);
    }

    /**
     * Get achievements in branch id
     *
     * @param gameId        game id
     * @param achievementId achievement id
     * @return ServiceResponse with a list of Achievement
     */
    @Override
    public ServiceResponse<List<Achievement>> getAchievementsInSameBranch(GameId gameId, Integer achievementId) {
        Achievement achievement = this.lambdaQuery()
                .eq(Achievement::getGameId, gameId)
                .eq(Achievement::getAchievementId, achievementId)
                .one();
        if (achievement == null) {
            return ServiceResponse.error("No achievement found with game id: " + gameId + ", achievement id: " + achievementId);
        }

        // Branch id only meaningful when it's greater than 0'
        if (achievement.getBranchId() <= 0) {
            return ServiceResponse.success("No achievements in branch id less or equal to 0", List.of());
        }

        List<Achievement> list = this.lambdaQuery()
                .eq(Achievement::getGameId, gameId)
                .eq(Achievement::getBranchId, achievement.getBranchId())
                .ne(Achievement::getAchievementId, achievementId)
                .list();
        if (list == null || list.isEmpty()) {
            return ServiceResponse.error("No achievements in branch id: " + achievement.getBranchId());
        }

        return ServiceResponse.success("Get achievements in branch id: " + achievement.getBranchId(), list);
    }

    /**
     * Insert achievement batch; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Override
    @Transactional
    public ServiceResponse<?> insertAchievementBatch(List<Map<String, Object>> achievementMapList) {
        List<Achievement> inserts = new ArrayList<>();

        for (Map<String, Object> achievementMap : achievementMapList) {
            Achievement achievement = BeanUtil.toBean(achievementMap, Achievement.class);

            try {
                // Check if all fields are filled
                if (MigrationUtils.hasNullFieldExcept(achievement, "rewardLevel", "branchId")) {
                    log.warn("Invalid achievement for insert: {}", achievementMap);
                    return ServiceResponse.error("Invalid achievement for insert.");
                }
            } catch (Exception e) {
                log.error("Error occurred while checking achievement fields: {}", e.getMessage());
                return ServiceResponse.error("Error occurred while checking achievement fields.");
            }

            inserts.add(achievement);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert achievement batch successfully.");
        return ServiceResponse.success("Insert achievement batch successfully.");
    }

    /**
     * Update achievements; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Override
    @Transactional
    public ServiceResponse<?> updateAchievementBatch(List<Map<String, Object>> achievementMapList) {
        for (Map<String, Object> achievementMap : achievementMapList) {
            // Get target id from the map
            Object target = achievementMap.get("target");
            if (target == null) {
                log.warn("Invalid achievement for update: missing 'target' for lookup.");
                return ServiceResponse.error("Invalid achievement for update: missing 'target' for lookup.");
            }
            if (!(target instanceof Map)) {
                log.warn("Invalid achievement for update: 'target' is not a map.");
                return ServiceResponse.error("Invalid achievement for update: 'target' is not a map.");
            }
            Map<String, Object> targetMap = (Map<String, Object>) target;
            GameId gameId = GameId.valueOf(targetMap.get("game_id").toString());
            Integer achievementId = Integer.valueOf(targetMap.get("achievement_id").toString());

            // Find target achievement
            Achievement achievement = this.lambdaQuery()
                    .eq(Achievement::getGameId, gameId)
                    .eq(Achievement::getAchievementId, achievementId)
                    .one();
            if (achievement == null) {
                log.warn("Invalid achievement for update: achievement not found for lookup.");
                return ServiceResponse.error("Invalid achievement for update: achievement not found for lookup.");
            }

            // Update target
            achievementMap.remove("target");
            BeanUtil.fillBeanWithMap(achievementMap, achievement,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreCase(true)
                            .setIgnoreError(true));

            // Update achievement
            GameId currGameId = achievement.getGameId();
            Integer currAchievementId = achievement.getAchievementId();
            UpdateWrapper<Achievement> updateWrapper = new UpdateWrapper<>();
            if (!currGameId.equals(gameId) || !currAchievementId.equals(achievementId)) {
                // Handle the situation that id is changed
                updateWrapper.eq("game_id", gameId);
                updateWrapper.eq("achievement_id", achievementId);
                updateWrapper.set("game_id", currGameId);
                updateWrapper.set("achievement_id", currAchievementId);
            } else {
                updateWrapper.eq("game_id", gameId);
                updateWrapper.eq("achievement_id", achievementId);
            }
            boolean success = this.update(achievement, updateWrapper);
            if (!success) {
                log.warn("Failed to update achievement for game: {} id: {}", gameId, achievementId);
                throw new RuntimeException("Failed to update achievement for game: " + gameId + " id: " + achievementId);
            }
        }

        log.debug("Update achievement batch successfully.");
        return ServiceResponse.success("Update achievement batch successfully.");
    }

    @Override
    public ServiceResponse<?> deleteAchievementBatch(List<Map<String, Object>> achievementMapList) {
        log.info("Delete achievement batch is not supported.");
        return null;
    }
}

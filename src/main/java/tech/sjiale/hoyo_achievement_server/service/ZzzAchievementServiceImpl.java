package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzAchievement;
import tech.sjiale.hoyo_achievement_server.mapper.ZzzAchievementMapper;

import java.util.ArrayList;
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
    public ServiceResponse<?> insertAchievementBatch(List<Map<String, Object>> achievementMapList) {
        List<ZzzAchievement> inserts = new ArrayList<>();

        for (Map<String, Object> achievementMap : achievementMapList) {
            ZzzAchievement zzzAchievement = BeanUtil.toBean(achievementMap, ZzzAchievement.class);

            // Check if all fields are filled
            if (BeanUtil.hasNullField(zzzAchievement)) {
                log.warn("Invalid ZZZ achievement data: {}", achievementMap);
                return ServiceResponse.error("Invalid ZZZ achievement data.");
            }

            inserts.add(zzzAchievement);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert ZZZ achievement batch successfully.");
        return ServiceResponse.success("Insert ZZZ achievement batch successfully.");
    }

    /**
     * Update ZZZ achievements; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateAchievementBatch(List<Map<String, Object>> achievementMapList) {
        List<ZzzAchievement> updates = new ArrayList<>();

        for (Map<String, Object> achievementMap : achievementMapList) {
            // Get record id from the map
            Object recordIdObj = achievementMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid ZZZ achievement data: missing 'record_id' for lookup.");
                return ServiceResponse.error("Invalid ZZZ achievement data: missing 'record_id' for lookup.");
            }
            Integer oldId = Integer.valueOf(recordIdObj.toString());

            // Find target achievement
            ZzzAchievement targetAchievement = this.getById(oldId);
            if (targetAchievement == null) {
                log.warn("No ZZZ achievement found with id: {}", oldId);
                return ServiceResponse.error("No ZZZ achievement found with id: " + oldId);
            }

            // Update target achievement
            BeanUtil.fillBeanWithMap(achievementMap, targetAchievement,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreCase(true)
                            .setIgnoreError(true)
            );

            // Handle the situation that id is changed
            Integer newId = targetAchievement.getAchievementId();
            if (!oldId.equals(newId)) {
                UpdateWrapper<ZzzAchievement> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("achievement_id", oldId);
                updateWrapper.set("achievement_id", newId);
                boolean success = this.update(targetAchievement, updateWrapper);
                if (!success) {
                    log.warn("Failed to update ZZZ achievement for id: {}", oldId);
                    throw new RuntimeException("Failed to update ZZZ achievement for id: " + oldId);
                }
            } else {
                // Save result to list
                updates.add(targetAchievement);
            }
        }

        // Save updates results to the database
        if (!updates.isEmpty()) {
            this.updateBatchById(updates);
        }

        log.debug("Update ZZZ achievements batch successfully.");
        return ServiceResponse.success("Update ZZZ achievements batch successfully.");
    }
}

package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.SrAchievement;
import tech.sjiale.hoyo_achievement_server.mapper.SrAchievementMapper;

import java.util.ArrayList;
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
     * Get all SR achievements
     *
     * @return ServiceResponse with a list of ZzzAchievement
     */
    public ServiceResponse<List<SrAchievement>> getAllAchievements() {
        return ServiceResponse.success("Get all SR achievements successfully.", this.list());
    }

    /**
     * Insert SR achievements; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertAchievementBatch(List<Map<String, Object>> achievementMapList) {
        List<SrAchievement> inserts = new ArrayList<>();

        for (Map<String, Object> achievementMap : achievementMapList) {
            SrAchievement srAchievement = BeanUtil.toBean(achievementMap, SrAchievement.class);

            // Check if all fields are filled
            if (BeanUtil.hasNullField(srAchievement)) {
                log.warn("Invalid SR achievement data: {}", achievementMap);
                throw new IllegalArgumentException("Invalid SR achievement data.");
            }

            inserts.add(srAchievement);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert SR achievement batch successfully.");
        return ServiceResponse.success("Insert SR achievement batch successfully.");
    }

    /**
     * Update SR achievements; should only be called by migration service
     *
     * @param achievementMapList List of achievement data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateAchievementBatch(List<Map<String, Object>> achievementMapList) {
        List<SrAchievement> updates = new ArrayList<>();

        for (Map<String, Object> achievementMap : achievementMapList) {
            // Get record id from the map
            Object recordIdObj = achievementMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid SR achievement: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid SR achievement: missing 'record_id' for lookup.");
            }
            Integer oldId = Integer.valueOf(recordIdObj.toString());

            // Find target achievement
            SrAchievement targetAchievement = this.getById(oldId);
            if (targetAchievement == null) {
                log.warn("No SR achievement found with id: {}", oldId);
                throw new IllegalArgumentException("No SR achievement found with id: " + oldId);
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
                UpdateWrapper<SrAchievement> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("achievement_id", oldId);
                updateWrapper.set("achievement_id", newId);
                boolean success = this.update(targetAchievement, updateWrapper);
                if (!success) {
                    log.warn("Failed to update SR achievement for id: {}", oldId);
                    throw new RuntimeException("Failed to update SR achievement for id: " + oldId);
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

        log.debug("Update SR achievements batch successfully.");
        return ServiceResponse.success("Update SR achievements batch successfully.");
    }
}

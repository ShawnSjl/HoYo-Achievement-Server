package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.SrBranch;
import tech.sjiale.hoyo_achievement_server.mapper.SrBranchMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("srBranchService")
public class SrBranchServiceImpl extends ServiceImpl<SrBranchMapper, SrBranch> implements SrBranchService {

    /**
     * Get all SR branches
     *
     * @return List of SR branches
     */
    public ServiceResponse<List<SrBranch>> getAllBranches() {
        List<SrBranch> branches = this.lambdaQuery()
                .orderByAsc(SrBranch::getBranchId)
                .list();
        if (branches == null || branches.isEmpty()) {
            return ServiceResponse.error("No SR branches found.");
        }
        return ServiceResponse.success("Get all SR branches successfully.", branches);
    }

    /**
     * Get all SR achievement ids in the same branch as the given achievement id; return an empty list if
     * the achievement is not in a branch
     *
     * @param achievementId achievement id
     * @return List of achievement ids
     */
    public ServiceResponse<List<Integer>> getAchievementInSameBranch(Integer achievementId) {
        // Get SR branche id by achievement id, return an empty list if the achievement is not in a branch
        Integer branchId = this.lambdaQuery()
                .eq(SrBranch::getAchievementId, achievementId)
                .oneOpt()
                .map(SrBranch::getBranchId)
                .orElse(null);
        if (branchId == null) {
            log.debug("No SR branch found for achievement id: {}", achievementId);
            return ServiceResponse.success("No SR branch found for achievement id: " + achievementId, List.of());
        }

        // Get other achievement ids in the same branch
        List<Integer> achievementIds = this.lambdaQuery()
                .select(SrBranch::getAchievementId)
                .eq(SrBranch::getBranchId, branchId)
                .ne(SrBranch::getAchievementId, achievementId)
                .list()
                .stream()
                .map(SrBranch::getAchievementId)
                .toList();
        log.debug("Get SR achievement ids in the same branch successfully.");
        return ServiceResponse.success("Get SR achievement ids in the same branch successfully.", achievementIds);
    }

    /**
     * Insert SR branches; should only be called by migration service
     *
     * @param branchMapList List of branch data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertBranchBatch(List<Map<String, Object>> branchMapList) {
        List<SrBranch> inserts = new ArrayList<>();

        for (Map<String, Object> branchMap : branchMapList) {
            SrBranch srBranch = BeanUtil.toBean(branchMap, SrBranch.class);

            // Check if all fields are filled
            if (BeanUtil.hasNullField(srBranch)) {
                log.warn("Invalid SR branch data: {}", branchMap);
                throw new IllegalArgumentException("Invalid SR branch data.");
            }

            inserts.add(srBranch);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert SR branches successfully.");
        return ServiceResponse.success("Insert SR branches successfully.");
    }

    /**
     * Update SR branches; should only be called by migration service
     *
     * @param branchMapList List of branch data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateBranchBatch(List<Map<String, Object>> branchMapList) {
        List<SrBranch> updates = new ArrayList<>();

        for (Map<String, Object> branchMap : branchMapList) {
            // Get record id from the map
            Object recordIdObj = branchMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid SR branch data: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid SR branch data: missing 'record_id' for lookup.");
            }
            Integer oldId = Integer.valueOf(recordIdObj.toString());

            // Find the target branch
            SrBranch targetBranch = this.getById(oldId);
            if (targetBranch == null) {
                log.warn("No SR branch found with id: {}", oldId);
                throw new IllegalArgumentException("No SR branch found with id: " + oldId);
            }

            // Update target branch
            BeanUtil.fillBeanWithMap(branchMap, targetBranch,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreCase(true)
                            .setIgnoreError(true)
            );

            // Handle the situation that id is changed
            Integer newId = targetBranch.getAchievementId();
            if (!oldId.equals(newId)) {
                UpdateWrapper<SrBranch> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("achievement_id", oldId);
                updateWrapper.set("achievement_id", newId);
                boolean success = this.update(targetBranch, updateWrapper);
                if (!success) {
                    log.warn("Failed to update SR branch for id: {}", oldId);
                    throw new RuntimeException("Failed to update SR branch for id: " + oldId);
                }
            } else {
                // Save result to list
                updates.add(targetBranch);
            }
        }

        // Save updates results to the database
        if (!updates.isEmpty()) {
            this.updateBatchById(updates);
        }

        log.debug("Update SR branches batch successfully.");
        return ServiceResponse.success("Update SR branches batch successfully.");
    }
}

package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzBranch;
import tech.sjiale.hoyo_achievement_server.mapper.ZzzBranchMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("zzzBranchService")
public class ZzzBranchServiceImpl extends ServiceImpl<ZzzBranchMapper, ZzzBranch> implements ZzzBranchService {

    /**
     * Get all ZZZ branches
     *
     * @return List of ZZZ branches
     */
    public ServiceResponse<List<ZzzBranch>> getAllBranches() {
        List<ZzzBranch> branches = this.lambdaQuery()
                .orderByAsc(ZzzBranch::getBranchId)
                .list();
        if (branches == null || branches.isEmpty()) {
            return ServiceResponse.error("No ZZZ branches found.");
        }
        return ServiceResponse.success("Get all ZZZ branches successfully.", branches);
    }

    /**
     * Get all ZZZ achievement ids in the same branch as the given achievement id; return an empty list if
     * the achievement is not in a branch
     *
     * @param achievementId achievement id
     * @return List of achievement ids
     */
    public ServiceResponse<List<Integer>> getAchievementInSameBranch(Integer achievementId) {
        // Get ZZZ branches by achievement id, return an empty list if the achievement is not in a branch
        Integer branchId = this.lambdaQuery()
                .eq(ZzzBranch::getAchievementId, achievementId)
                .oneOpt()
                .map(ZzzBranch::getBranchId)
                .orElse(null);
        if (branchId == null) {
            log.debug("No ZZZ branch found for achievement id: {}", achievementId);
            return ServiceResponse.success("No ZZZ branch found for achievement id: " + achievementId, List.of());
        }

        // Get other achievement ids in the same branch
        List<Integer> achievementIds = this.lambdaQuery()
                .select(ZzzBranch::getAchievementId)
                .eq(ZzzBranch::getBranchId, branchId)
                .ne(ZzzBranch::getAchievementId, achievementId)
                .list()
                .stream()
                .map(ZzzBranch::getAchievementId)
                .toList();
        log.debug("Get ZZZ achievement ids in the same branch successfully.");
        return ServiceResponse.success("Get ZZZ achievement ids in the same branch successfully.", achievementIds);
    }

    /**
     * Insert ZZZ branches; should only be called by migration service
     *
     * @param branchMapList List of branch data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertBranchBatch(List<Map<String, Object>> branchMapList) {
        List<ZzzBranch> inserts = new ArrayList<>();

        for (Map<String, Object> branchMap : branchMapList) {
            ZzzBranch zzzBranch = BeanUtil.toBean(branchMap, ZzzBranch.class);

            // Check if all fields are filled
            if (BeanUtil.hasNullField(zzzBranch)) {
                log.warn("Invalid ZZZ branch data: {}", branchMap);
                throw new IllegalArgumentException("Invalid ZZZ branch data.");
            }

            inserts.add(zzzBranch);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert ZZZ branch batch successfully.");
        return ServiceResponse.success("Insert ZZZ branch batch successfully.");
    }

    /**
     * Update ZZZ branches; should only be called by migration service
     *
     * @param branchMapList List of branch data
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateBranchBatch(List<Map<String, Object>> branchMapList) {
        List<ZzzBranch> updates = new ArrayList<>();

        for (Map<String, Object> branchMap : branchMapList) {
            // Get record id from the map
            Object recordIdObj = branchMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid ZZZ branch data: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid ZZZ branch data: missing 'record_id' for lookup.");
            }
            Integer oldId = Integer.valueOf(recordIdObj.toString());

            // Find the target branch
            ZzzBranch targetBranch = this.getById(oldId);
            if (targetBranch == null) {
                log.warn("No ZZZ branch found with id: {}", oldId);
                throw new IllegalArgumentException("No ZZZ branch found with id: " + oldId);
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
                UpdateWrapper<ZzzBranch> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("achievement_id", oldId);
                updateWrapper.set("achievement_id", newId);
                boolean success = this.update(targetBranch, updateWrapper);
                if (!success) {
                    log.warn("Failed to update ZZZ branch for id: {}", oldId);
                    throw new RuntimeException("Failed to update ZZZ branch for id: " + oldId);
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

        log.debug("Update ZZZ branches batch successfully.");
        return ServiceResponse.success("Update ZZZ branches batch successfully.");
    }
}

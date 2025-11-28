package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.SrBranch;
import tech.sjiale.hoyo_achievement_server.mapper.SrBranchMapper;

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
    public ServiceResponse<?> insertBranches(List<Map<String, Object>> branchMapList) {
        for (Map<String, Object> branchMap : branchMapList) {
            Integer achievementId = (Integer) branchMap.get("achievement_id");
            Integer branchId = (Integer) branchMap.get("branch_id");

            if (achievementId == null || branchId == null) {
                log.error("Invalid branch data: achievement_id={}, branch_id={}", achievementId, branchId);
                throw new IllegalArgumentException("Invalid branch data.");
            }

            SrBranch branch = new SrBranch();
            branch.setAchievementId(achievementId);
            branch.setBranchId(branchId);
            this.save(branch);
        }
        log.debug("Insert SR branches successfully.");
        return ServiceResponse.success("Insert SR branches successfully.");
    }
}

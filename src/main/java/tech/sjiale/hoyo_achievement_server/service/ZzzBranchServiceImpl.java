package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzBranch;
import tech.sjiale.hoyo_achievement_server.mapper.ZzzBranchMapper;

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
        List<ZzzBranch> branches = this.list();
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
    public ServiceResponse<?> insertBranches(List<Map<String, Object>> branchMapList) {
        for (Map<String, Object> branchMap : branchMapList) {
            Integer achievementId = (Integer) branchMap.get("achievement_id");
            Integer branchId = (Integer) branchMap.get("branch_id");

            if (achievementId == null || branchId == null) {
                log.error("Invalid branch data: achievement_id={}, branch_id={}", achievementId, branchId);
                throw new IllegalArgumentException("Invalid branch data.");
            }

            ZzzBranch branch = new ZzzBranch();
            branch.setAchievementId(achievementId);
            branch.setBranchId(branchId);
            this.save(branch);
        }
        log.debug("Insert ZZZ branches successfully.");
        return ServiceResponse.success("Insert ZZZ branches successfully.");
    }
}

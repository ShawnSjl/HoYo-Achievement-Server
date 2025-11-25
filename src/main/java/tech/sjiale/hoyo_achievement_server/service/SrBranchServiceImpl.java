package tech.sjiale.hoyo_achievement_server.service;

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
     */
    public ServiceResponse<List<SrBranch>> getAllBranches() {
        List<SrBranch> branches = this.list();

        if (branches == null || branches.isEmpty()) {
            log.error("No SR branches found.");
            return ServiceResponse.error("No SR branches found.");
        }

        log.debug("Get all SR branches successfully.");
        return ServiceResponse.success("Get all SR branches successfully.", branches);
    }

    /**
     * Get all SR achievement ids in the same branch as the given achievement id; return empty list if achievement is
     * not in a branch
     */
    public ServiceResponse<List<Integer>> getAchievementInSameBranch(Integer achievementId) {
        // Get SR branches by achievement id, return empty list if achievement is not in a branch
        SrBranch srBranch = this.lambdaQuery().eq(SrBranch::getAchievement_id, achievementId).one();
        if (srBranch == null) {
            log.debug("No SR branch found for achievement id: {}", achievementId);
            List<Integer> list = new ArrayList<>();
            return ServiceResponse.success("No SR branch found for achievement id: " + achievementId, list);
        }

        // Get other achievement ids in the same branch
        List<Integer> achievementIds = this.lambdaQuery()
                .select(SrBranch::getAchievement_id)
                .eq(SrBranch::getBranch_id, srBranch.getBranch_id())
                .ne(SrBranch::getAchievement_id, achievementId)
                .list()
                .stream()
                .map(SrBranch::getAchievement_id)
                .toList();
        log.debug("Get SR achievement ids in the same branch successfully.");
        return ServiceResponse.success("Get SR achievement ids in the same branch successfully.", achievementIds);
    }

    /**
     * Insert SR branches; should only be called by migration service
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
            branch.setAchievement_id(achievementId);
            branch.setBranch_id(branchId);
            this.save(branch);
        }
        log.debug("Insert SR branches successfully.");
        return ServiceResponse.success("Insert SR branches successfully.");
    }
}

package tech.sjiale.hoyo_achievement_server.service;

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
     */
    public ServiceResponse<List<ZzzBranch>> getAllBranches() {
        List<ZzzBranch> branches = this.list();

        if (branches == null || branches.isEmpty()) {
            log.error("No ZZZ branches found.");
            return ServiceResponse.error("No ZZZ branches found.");
        }

        log.debug("Get all ZZZ branches successfully.");
        return ServiceResponse.success("Get all ZZZ branches successfully.", branches);
    }

    /**
     * Get all ZZZ achievement ids in the same branch as the given achievement id; return empty list if achievement is
     * not in a branch
     */
    public ServiceResponse<List<Integer>> getAchievementInSameBranch(Integer achievementId) {
        // Get ZZZ branches by achievement id, return empty list if achievement is not in a branch
        ZzzBranch zzzBranch = this.lambdaQuery().eq(ZzzBranch::getAchievement_id, achievementId).one();
        if (zzzBranch == null) {
            log.debug("No ZZZ branch found for achievement id: {}", achievementId);
            List<Integer> list = new ArrayList<>();
            return ServiceResponse.success("No ZZZ branch found for achievement id: " + achievementId, list);
        }

        // Get other achievement ids in the same branch
        List<Integer> achievementIds = this.lambdaQuery()
                .select(ZzzBranch::getAchievement_id)
                .eq(ZzzBranch::getBranch_id, zzzBranch.getBranch_id())
                .ne(ZzzBranch::getAchievement_id, achievementId)
                .list()
                .stream()
                .map(ZzzBranch::getAchievement_id)
                .toList();
        log.debug("Get ZZZ achievement ids in the same branch successfully.");
        return ServiceResponse.success("Get ZZZ achievement ids in the same branch successfully.", achievementIds);
    }

    /**
     * Insert ZZZ branches; should only be called by migration service
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
            branch.setAchievement_id(achievementId);
            branch.setBranch_id(branchId);
            this.save(branch);
        }
        log.debug("Insert ZZZ branches successfully.");
        return ServiceResponse.success("Insert ZZZ branches successfully.");
    }
}

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
     * Insert ZZZ branches; should only be called by migration service
     */
    @Transactional
    public ServiceResponse<?> insertBranches(List<Map<String, Object>> branchMapList) {
        for (Map<String, Object> branchMap : branchMapList) {
            Integer achievementId = (Integer) branchMap.get("achievement_id");
            Integer branchId = (Integer) branchMap.get("branch_id");

            if (achievementId == null || branchId == null) {
                log.error("Invalid branch data: achievement_id={}, branch_id={}", achievementId, branchId);
                return ServiceResponse.error("Invalid branch data.");
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

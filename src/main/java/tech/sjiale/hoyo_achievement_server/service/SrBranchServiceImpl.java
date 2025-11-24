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
public class SrBranchServiceImpl extends ServiceImpl<SrBranchMapper, SrBranch> implements SrBranchService{

    /** Get all SR branches */
    public ServiceResponse<List<SrBranch>> getAllBranches() {
        List<SrBranch> branches = this.list();
        log.debug("Get all SR branches successfully.");
        return ServiceResponse.success("Get all SR branches successfully.", branches);
    }

    /** Insert SR branches; should only be called by migration service */
    @Transactional
    public ServiceResponse<?> insertBranches(List<Map<String, Object>> branchMapList) {
        for (Map<String, Object> branchMap : branchMapList) {
            Integer achievementId = (Integer) branchMap.get("achievement_id");
            Integer branchId = (Integer) branchMap.get("branch_id");

            if (achievementId == null || branchId == null) {
                log.error("Invalid branch data: achievement_id={}, branch_id={}", achievementId, branchId);
                return ServiceResponse.error("Invalid branch data.");
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

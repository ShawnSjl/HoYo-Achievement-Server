package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ZzzBranch;

import java.util.List;
import java.util.Map;

public interface ZzzBranchService extends IService<ZzzBranch> {
    ServiceResponse<List<ZzzBranch>> getAllBranches();

    ServiceResponse<List<Integer>> getAchievementInSameBranch(Integer achievementId);

    // Data migration use
    ServiceResponse<?> insertBranchBatch(List<Map<String, Object>> branchMapList);

    ServiceResponse<?> updateBranchBatch(List<Map<String, Object>> branchMapList);

    ServiceResponse<?> deleteBranchBatch(List<Map<String, Object>> branchMapList);
}

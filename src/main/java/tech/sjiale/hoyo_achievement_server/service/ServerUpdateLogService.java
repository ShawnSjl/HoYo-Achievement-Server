package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerUpdateLog;

import java.util.List;
import java.util.Map;

public interface ServerUpdateLogService extends IService<ServerUpdateLog> {
    ServiceResponse<List<ServerUpdateLog>> getAllServerUpdateLog();

    ServiceResponse<List<ServerUpdateLog>> getLatestServerUpdateLog(Long logID);

    // Data migration use
    ServiceResponse<?> insertServerUpdateLogBatch(List<Map<String, Object>> serverUpdateLogMapList);

    ServiceResponse<?> updateServerUpdateLogBatch(List<Map<String, Object>> serverUpdateLogMapList);

    ServiceResponse<?> deleteServerUpdateLogBatch(List<Map<String, Object>> serverUpdateLogMapList);
}

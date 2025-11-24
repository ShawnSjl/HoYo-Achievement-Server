package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerInfo;

import java.util.List;
import java.util.Map;

public interface ServerInfoService extends IService<ServerInfo> {
    ServiceResponse<ServerInfo> getServerInfoById(Integer id);
    ServiceResponse<ServerInfo> getLatestServerInfo();
    ServiceResponse<?> insertServerInfoBatch(List<Map<String, Object>> serverInfoMapList);
}

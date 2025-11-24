package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerInfo;
import tech.sjiale.hoyo_achievement_server.mapper.ServerInfoMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("serverInfoService")
public class ServerInfoServiceImpl extends ServiceImpl<ServerInfoMapper, ServerInfo> implements ServerInfoService{

    /** Get the server info by id; if id is invalid, return the latest server info */
    public ServiceResponse<ServerInfo> getServerInfoById(Integer id) {
        if (id == null || id <= 0) {
            log.error("id is invalid: {}", id);
            return this.getLatestServerInfo();
        }
        return ServiceResponse.success("Get server info successfully.", this.getById(id));
    }

    /** Get the latest server info */
    public ServiceResponse<ServerInfo> getLatestServerInfo() {
        ServerInfo res = this.lambdaQuery().orderByDesc(ServerInfo::getInfo_id).last("LIMIT 1").one();
        if (res == null) {
            log.error("No server info found.");
            return ServiceResponse.error("No server info found.");
        }
        return ServiceResponse.success("Get server info successfully.", res);
    }

    @Transactional
    public ServiceResponse<?> insertServerInfoBatch(List<Map<String, Object>> serverInfoMapList) {
        for (Map<String, Object> serverInfoMap : serverInfoMapList) {
            String serverVersion = serverInfoMap.get("server_version").toString();
            String zzzVersion = serverInfoMap.get("zzz_version").toString();
            String srVersion = serverInfoMap.get("sr_version").toString();
            String updateDescription = serverInfoMap.get("update_description").toString();

            if (serverVersion == null || zzzVersion == null || srVersion == null || updateDescription == null) {
                log.error("Invalid server info: server_version={}, zzz_version={}, sr_version={}, update_description={}", serverVersion, zzzVersion, srVersion, updateDescription);
                return ServiceResponse.error("Invalid server info data.");
            }

            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServer_version(serverVersion);
            serverInfo.setZzz_version(zzzVersion);
            serverInfo.setSr_version(srVersion);
            serverInfo.setUpdate_description(updateDescription);
            this.save(serverInfo);
        }
        log.info("Insert server info batch successfully.");
        return ServiceResponse.success("Insert server info batch successfully.");
    }
}

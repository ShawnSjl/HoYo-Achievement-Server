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
public class ServerInfoServiceImpl extends ServiceImpl<ServerInfoMapper, ServerInfo> implements ServerInfoService {

    /**
     * Get all server info
     *
     * @return ServiceResponse with a list of ServerInfo
     */
    public ServiceResponse<List<ServerInfo>> getAllServerInfo() {
        List<ServerInfo> list = this.list();
        if (list == null || list.isEmpty()) {
            return ServiceResponse.error("No server info found.");
        }
        return ServiceResponse.success("Get all server info successfully.", list);
    }

    /**
     * Get the latest server info
     *
     * @return ServiceResponse with ServerInfo object
     */
    public ServiceResponse<ServerInfo> getLatestServerInfo() {
        ServerInfo res = this.lambdaQuery()
                .orderByDesc(ServerInfo::getInfoId)
                .last("LIMIT 1")
                .one();
        if (res == null) {
            return ServiceResponse.error("No server info found.");
        }
        return ServiceResponse.success("Get latest server info successfully.", res);
    }

    /**
     * Insert server info batch; should only be called by migration service
     *
     * @param serverInfoMapList list of the server info map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertServerInfoBatch(List<Map<String, Object>> serverInfoMapList) {
        for (Map<String, Object> serverInfoMap : serverInfoMapList) {
            String serverVersion = serverInfoMap.get("server_version").toString();
            String zzzVersion = serverInfoMap.get("zzz_version").toString();
            String srVersion = serverInfoMap.get("sr_version").toString();
            String updateDescription = serverInfoMap.get("update_description").toString();

            if (serverVersion == null || zzzVersion == null || srVersion == null || updateDescription == null) {
                log.error("Invalid server info: server_version={}, zzz_version={}, sr_version={}, update_description={}", serverVersion, zzzVersion, srVersion, updateDescription);
                throw new IllegalArgumentException("Invalid server info.");
            }

            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServerVersion(serverVersion);
            serverInfo.setZzzVersion(zzzVersion);
            serverInfo.setSrVersion(srVersion);
            serverInfo.setUpdateDescription(updateDescription);
            this.save(serverInfo);
        }
        log.debug("Insert server info batch successfully.");
        return ServiceResponse.success("Insert server info batch successfully.");
    }
}

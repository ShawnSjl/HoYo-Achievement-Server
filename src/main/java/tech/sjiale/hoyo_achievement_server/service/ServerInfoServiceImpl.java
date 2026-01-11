package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerInfo;
import tech.sjiale.hoyo_achievement_server.mapper.ServerInfoMapper;

import java.util.ArrayList;
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
        List<ServerInfo> inserts = new ArrayList<>();

        for (Map<String, Object> serverInfoMap : serverInfoMapList) {
            ServerInfo serverInfo = BeanUtil.toBean(serverInfoMap, ServerInfo.class);

            // Check if all fields are filled
            if (BeanUtil.hasNullField(serverInfo)) {
                log.warn("Invalid server info: {} for insert", serverInfoMap);
                throw new IllegalArgumentException("Invalid server info for insert.");
            }

            inserts.add(serverInfo);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert server info batch successfully.");
        return ServiceResponse.success("Insert server info batch successfully.");
    }

    /**
     * Update server info batch; should only be called by migration service
     *
     * @param serverInfoMapList list of the server info map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateServerInfoBatch(List<Map<String, Object>> serverInfoMapList) {
        List<ServerInfo> updates = new ArrayList<>();

        for (Map<String, Object> serverInfoMap : serverInfoMapList) {
            // Get record id from the map
            Object recordIdObj = serverInfoMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid server info: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid server info: missing 'record_id' for lookup.");
            }
            Long oldId = Long.valueOf(recordIdObj.toString());

            // Find target server info
            ServerInfo targetInfo = this.getById(oldId);
            if (targetInfo == null) {
                log.warn("No server info found with id: {}", oldId);
                throw new IllegalArgumentException("No server info found with id: " + oldId);
            }

            // Update target info
            BeanUtil.fillBeanWithMap(serverInfoMap, targetInfo,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreCase(true)
                            .setIgnoreError(true)
            );

            // Handle the situation that id is changed
            Long newId = targetInfo.getInfoId();
            if (!oldId.equals(newId)) {
                UpdateWrapper<ServerInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("info_id", oldId);
                updateWrapper.set("info_id", newId);
                boolean success = this.update(targetInfo, updateWrapper);
                if (!success) {
                    log.warn("Failed to update server info for id: {}", oldId);
                    throw new RuntimeException("Failed to update server info for id: " + oldId);
                }
            } else {
                // Save result to list
                updates.add(targetInfo);
            }
        }

        // Save updates results to the database
        if (!updates.isEmpty()) {
            this.updateBatchById(updates);
        }

        log.debug("Update server info batch successfully.");
        return ServiceResponse.success("Update server info batch successfully.");
    }
}

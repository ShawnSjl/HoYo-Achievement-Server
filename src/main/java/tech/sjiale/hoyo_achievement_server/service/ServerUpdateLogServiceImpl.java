package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerUpdateLog;
import tech.sjiale.hoyo_achievement_server.mapper.ServerUpdateLogMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service("serverUpdateLogService")
public class ServerUpdateLogServiceImpl extends ServiceImpl<ServerUpdateLogMapper, ServerUpdateLog> implements ServerUpdateLogService {

    /**
     * Get all server update log
     *
     * @return ServiceResponse with a list of ServerInfo
     */
    public ServiceResponse<List<ServerUpdateLog>> getAllServerUpdateLog() {
        List<ServerUpdateLog> list = this.list();
        if (list == null || list.isEmpty()) {
            return ServiceResponse.error("No server update log found.");
        }
        return ServiceResponse.success("Get all server update log successfully.", list);
    }

    /**
     * Get the latest server update logs
     *
     * @return ServiceResponse with ServerInfo object
     */
    public ServiceResponse<List<ServerUpdateLog>> getLatestServerUpdateLog(Long logID) {
        List<ServerUpdateLog> res = this.lambdaQuery()
                .orderByDesc(ServerUpdateLog::getId)
                .gt(ServerUpdateLog::getId, logID) // Where id > logID
                .list();
        if (res == null) {
            return ServiceResponse.error("No server update log found.");
        }
        return ServiceResponse.success("Get latest server update logs successfully.", res);
    }

    /**
     * Insert server update log batch; should only be called by migration service
     *
     * @param serverUpdateLogMapList list of the server update log map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertServerUpdateLogBatch(List<Map<String, Object>> serverUpdateLogMapList) {
        List<ServerUpdateLog> inserts = new ArrayList<>();

        for (Map<String, Object> serverUpdateLogMap : serverUpdateLogMapList) {
            ServerUpdateLog serverUpdateLog = BeanUtil.toBean(serverUpdateLogMap, ServerUpdateLog.class);

            // Check if all fields are filled
            boolean hasMissingFields = Stream.of(
                    serverUpdateLog.getServerVersion(),
                    serverUpdateLog.getUpdateDescription()
            ).anyMatch(Objects::isNull);
            if (hasMissingFields) {
                log.warn("Invalid server update log for insert: {}", serverUpdateLogMap);
                throw new IllegalArgumentException("Invalid server update log for insert.");
            }

            inserts.add(serverUpdateLog);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert server update log batch successfully.");
        return ServiceResponse.success("Insert server update log batch successfully.");
    }

    /**
     * Update server update log batch; should only be called by migration service
     *
     * @param serverUpdateLogMapList list of the server update log map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateServerUpdateLogBatch(List<Map<String, Object>> serverUpdateLogMapList) {
        List<ServerUpdateLog> updates = new ArrayList<>();

        for (Map<String, Object> serverUpdateLogMap : serverUpdateLogMapList) {
            // Get record id from the map
            Object recordIdObj = serverUpdateLogMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid server update log for update: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid server update log for update: missing 'record_id' for lookup.");
            }
            Long oldId = Long.valueOf(recordIdObj.toString());

            // Find target server update log
            ServerUpdateLog targetLog = this.getById(oldId);
            if (targetLog == null) {
                log.warn("No server update log found with id: {}", oldId);
                throw new IllegalArgumentException("No server update log found with id: " + oldId);
            }

            // Update target update log
            BeanUtil.fillBeanWithMap(serverUpdateLogMap, targetLog,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreCase(true)
                            .setIgnoreError(true)
            );

            // Handle the situation that id is changed
            Long newId = targetLog.getId();
            if (!oldId.equals(newId)) {
                UpdateWrapper<ServerUpdateLog> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", oldId);
                updateWrapper.set("id", newId);
                boolean success = this.update(targetLog, updateWrapper);
                if (!success) {
                    log.warn("Failed to update server update log for id: {}", oldId);
                    throw new RuntimeException("Failed to update server update log for id: " + oldId);
                }
            } else {
                // Save result to list
                updates.add(targetLog);
            }
        }

        // Save updates results to the database
        if (!updates.isEmpty()) {
            this.updateBatchById(updates);
        }

        log.debug("Update server update log batch successfully.");
        return ServiceResponse.success("Update server update log batch successfully.");
    }

    /**
     * Delete server update log batch; should only be called by migration service
     *
     * @param serverUpdateLogMapList List of the server update log map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> deleteServerUpdateLogBatch(List<Map<String, Object>> serverUpdateLogMapList) {
        List<Long> ids = new ArrayList<>();

        for (Map<String, Object> serverUpdateLogMap : serverUpdateLogMapList) {
            Object recordIdObj = serverUpdateLogMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid server update log for delete: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid server update log for delete: missing 'record_id' for lookup.");
            }
            ids.add(Long.valueOf(recordIdObj.toString()));
        }

        if (ids.isEmpty()) {
            log.warn("No server update log found for delete.");
            throw new IllegalArgumentException("No server update log found for delete.");
        }

        // Delete records
        boolean success = this.removeByIds(ids);
        if (success) {
            log.debug("Delete server update log batch successfully.");
            return ServiceResponse.success("Delete server update log batch successfully.");
        } else {
            log.warn("Delete server update log batch failed.");
            throw new RuntimeException("Delete server update log batch failed.");
        }
    }
}

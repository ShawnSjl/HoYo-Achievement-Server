package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataChangeLog;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;
import tech.sjiale.hoyo_achievement_server.mapper.DataChangeLogMapper;

import java.util.List;

@Slf4j
@Service("dataChangeLogService")
public class DataChangeLogServiceImpl extends ServiceImpl<DataChangeLogMapper, DataChangeLog> implements DataChangeLogService {

    @Override
    public ServiceResponse<?> addChangeLog(Long userId, ChangeEntityType entityType, String entityId, ChangeAction action) {
        DataChangeLog dataChangeLog = new DataChangeLog();
        dataChangeLog.setUserId(userId);
        dataChangeLog.setEntityType(entityType);
        dataChangeLog.setEntityId(entityId);
        dataChangeLog.setAction(action);
        this.save(dataChangeLog);
        log.debug("Add change log successfully: {}", dataChangeLog);
        return ServiceResponse.success("Add change log successfully.");
    }

    public ServiceResponse<?> getVersionChanges(Long currVersion) {
        List<DataChangeLog> logs = this.lambdaQuery()
                .gt(DataChangeLog::getVersion, currVersion) // Where version > currVersion
                .list();

        // TODO: not implement yet
        return null;
    }
}

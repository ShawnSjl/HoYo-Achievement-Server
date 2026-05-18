package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataChangeLog;
import tech.sjiale.hoyo_achievement_server.mapper.DataChangeLogMapper;

import java.util.List;

@Slf4j
@Service("dataChangeLogService")
public class DataChangeLogServiceImpl extends ServiceImpl<DataChangeLogMapper, DataChangeLog> implements DataChangeLogService {

    public ServiceResponse<?> getVersionChanges(Long currVersion) {
        List<DataChangeLog> logs = this.lambdaQuery()
                .gt(DataChangeLog::getVersion, currVersion) // Where version > currVersion
                .list();

        // TODO: not implement yet
        return null;
    }
}

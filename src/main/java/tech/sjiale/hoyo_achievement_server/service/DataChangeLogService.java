package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataChangeLog;

public interface DataChangeLogService extends IService<DataChangeLog> {
    ServiceResponse<?> getVersionChanges(Long currVersion);
}

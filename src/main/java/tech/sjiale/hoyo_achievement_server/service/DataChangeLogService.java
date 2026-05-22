package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataChangeLog;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;

public interface DataChangeLogService extends IService<DataChangeLog> {
    ServiceResponse<?> addChangeLog(Long userId, ChangeEntityType entityType, String entityId, ChangeAction action);

    ServiceResponse<?> getVersionChanges(Long currVersion);
}

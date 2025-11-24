package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataMigration;

public interface MigrationService extends IService<DataMigration> {
    ServiceResponse<?> importNewData(String path);

    boolean handleJSONFile(String jsonFile);
}

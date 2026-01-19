package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.MigrationResult;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataMigration;

import java.util.List;

public interface MigrationService extends IService<DataMigration> {
    ServiceResponse<List<DataMigration>> getAllMigrationRecord();

    ServiceResponse<List<MigrationResult>> importNewData();

    MigrationResult handleJSONFile(String jsonFile);
}

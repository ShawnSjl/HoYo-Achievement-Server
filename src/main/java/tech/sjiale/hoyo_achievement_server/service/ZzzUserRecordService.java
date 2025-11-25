package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.ZzzAchievementRecordDto;
import tech.sjiale.hoyo_achievement_server.entity.ZzzUserRecord;

import java.util.List;

public interface ZzzUserRecordService extends IService<ZzzUserRecord> {
    ServiceResponse<List<ZzzAchievementRecordDto>> getAllAchievementsEmptyRecord();

    ServiceResponse<List<ZzzAchievementRecordDto>> getAllAchievementsRecordByUUID(String uuid);
    
    ServiceResponse<Boolean> updateRecordById(String uuid, Integer achievementId, Integer completeStatus);
}

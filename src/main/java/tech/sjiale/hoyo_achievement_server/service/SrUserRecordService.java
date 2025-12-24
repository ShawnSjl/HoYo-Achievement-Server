package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.SrUserRecord;

import java.util.List;

public interface SrUserRecordService extends IService<SrUserRecord> {
    ServiceResponse<List<SrUserRecord>> getAllRecordByUUID(String uuid);

    ServiceResponse<?> updateRecordById(String uuid, Integer achievementId, Integer completeStatus);
}

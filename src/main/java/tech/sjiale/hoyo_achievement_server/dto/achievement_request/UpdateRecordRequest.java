package tech.sjiale.hoyo_achievement_server.dto.achievement_request;

import lombok.Data;

@Data
public class UpdateRecordRequest {
    private String uuid;
    private Integer achievementId;
    private Integer completeStatus;
}

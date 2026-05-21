package tech.sjiale.hoyo_achievement_server.dto.achievement_request;

import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

@Data
public class UpdateRecordRequest {
    private String uuid;
    private GameId gameId;
    private Integer achievementId;
    private Integer completeStatus;
}

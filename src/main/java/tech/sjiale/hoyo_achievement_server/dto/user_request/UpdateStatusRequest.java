package tech.sjiale.hoyo_achievement_server.dto.user_request;

import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;

@Data
public class UpdateStatusRequest {
    private Long userId;
    private UserStatus status;
}

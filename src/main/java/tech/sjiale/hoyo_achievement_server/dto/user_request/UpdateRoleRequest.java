package tech.sjiale.hoyo_achievement_server.dto.user_request;

import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;

@Data
public class UpdateRoleRequest {
    private Long userId;
    private UserRole role;
}

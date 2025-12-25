package tech.sjiale.hoyo_achievement_server.dto.user_request;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

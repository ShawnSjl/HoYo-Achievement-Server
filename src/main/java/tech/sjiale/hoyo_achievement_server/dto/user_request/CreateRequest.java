package tech.sjiale.hoyo_achievement_server.dto.user_request;

import lombok.Data;

@Data
public class CreateRequest {
    private String username;
    private String password;
}

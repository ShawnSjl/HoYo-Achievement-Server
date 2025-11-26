package tech.sjiale.hoyo_achievement_server.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}

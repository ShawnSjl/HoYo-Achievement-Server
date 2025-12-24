package tech.sjiale.hoyo_achievement_server.dto.user_request;

import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;

import java.time.LocalDateTime;

@Data
public class UserExposeDto {
    private Long id;
    private String username;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

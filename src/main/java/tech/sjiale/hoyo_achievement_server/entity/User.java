package tech.sjiale.hoyo_achievement_server.entity;

import tech.sjiale.hoyo_achievement_server.entity.nume.*;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private UserRole role;
    private UserStatus status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime created_at = LocalDateTime.now();

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated_at = LocalDateTime.now();
}

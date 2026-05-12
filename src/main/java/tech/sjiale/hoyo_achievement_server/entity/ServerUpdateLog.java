package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("server_update_log")
public class ServerUpdateLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "server_version")
    private String serverVersion;

    @TableField(value = "update_description")
    private String updateDescription;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}

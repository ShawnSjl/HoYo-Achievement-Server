package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("server_info")
public class ServerInfo {
    @TableId(value = "info_id", type = IdType.AUTO)
    private Long infoId;

    @TableField(value = "server_version")
    private String serverVersion;

    @TableField(value = "zzz_version")
    private String zzzVersion;

    @TableField(value = "sr_version")
    private String srVersion;

    @TableField(value = "update_description")
    private String updateDescription;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}

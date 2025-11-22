package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("server_info")
public class ServerInfo {
    @TableId(type = IdType.AUTO)
    private Long info_id;

    private String server_version;
    private String zzz_version;
    private String sr_version;
    private String update_description;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime update_at;
}

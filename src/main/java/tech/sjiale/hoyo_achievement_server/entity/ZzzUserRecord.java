package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("zzz_user_record")
public class ZzzUserRecord {
    private String account_uuid;
    private Integer achievement_id;
    private Integer complete;
}

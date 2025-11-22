package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sr_user_record")
public class SrUserRecord {
    private String account_uuid;
    private Integer achievement_id;
    private Integer complete;
}

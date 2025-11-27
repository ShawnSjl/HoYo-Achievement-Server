package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sr_user_record")
public class SrUserRecord {
    @TableField(value = "account_uuid")
    private String accountUuid;

    @TableField(value = "achievement_id")
    private Integer achievementId;

    @TableField(value = "complete")
    private Integer complete;
}

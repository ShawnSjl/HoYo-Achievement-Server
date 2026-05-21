package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

@Data
@TableName("user_record")
public class UserRecord {
    @TableField(value = "account_uuid")
    private String accountUuid;

    @TableField(value = "game_id")
    private GameId gameId;

    @TableField(value = "achievement_id")
    private Integer achievementId;

    @TableField(value = "complete")
    private Integer complete;
}

package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

@Data
@TableName("account")
public class Account {
    @TableId(value = "account_uuid")
    private String accountUuid;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "game_id")
    private GameId gameId;

    @TableField(value = "account_name")
    private String accountName;

    @TableField(value = "account_in_game_uid")
    private String accountInGameUid;
}

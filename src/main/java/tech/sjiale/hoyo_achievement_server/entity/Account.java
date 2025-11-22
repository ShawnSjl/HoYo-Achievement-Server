package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameType;

@Data
@TableName("account")
public class Account {
    private String account_uuid;
    private Long user_id;
    private GameType game_type;
    private String account_name;
    private String account_in_game_uid;
}

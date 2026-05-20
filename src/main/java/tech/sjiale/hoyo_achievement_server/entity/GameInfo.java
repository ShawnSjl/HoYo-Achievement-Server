package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

@Data
@TableName("game_info")
public class GameInfo {
    @TableId(value = "game_id")
    private GameId gameId;

    @TableField(value = "game_version")
    private String gameVersion;
}

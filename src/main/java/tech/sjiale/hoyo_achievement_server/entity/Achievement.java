package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;

@Data
@TableName("achievement")
public class Achievement {
    @TableField(value = "game_id")
    private GameId gameId;

    @TableField(value = "achievement_id")
    private Integer achievementId;

    @TableField(value = "name")
    private String name;

    @TableField(value = "category")
    private String category;

    @TableField(value = "description")
    private String description;

    @TableField(value = "reward_level")
    private Integer rewardLevel;

    @TableField(value = "branch_id")
    private Integer branchId;

    @TableField(value = "game_version")
    private String gameVersion;
}

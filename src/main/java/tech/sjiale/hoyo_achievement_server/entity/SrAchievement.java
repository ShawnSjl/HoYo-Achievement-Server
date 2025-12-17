package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sr_achievement")
public class SrAchievement {
    @TableId(value = "achievement_id")
    private Integer achievementId;

    @TableField(value = "class_name")
    private String className;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "reward_level")
    private Integer rewardLevel;

    @TableField(value = "game_version")
    private String gameVersion;
}

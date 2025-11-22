package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sr_achievement")
public class SrAchievement {
    private Integer achievement_id;

    @TableField("class")
    private String className;

    private String name;
    private String description;
    private Integer reward_level;
    private Integer hidden;
    private String game_version;
}

package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("zzz_achievement")
public class ZzzAchievement {
    private Integer achievement_id;
    private Integer class_id;
    private String name;
    private String description;
    private Integer reward_level;
    private Integer hidden;
    private String game_version;
}

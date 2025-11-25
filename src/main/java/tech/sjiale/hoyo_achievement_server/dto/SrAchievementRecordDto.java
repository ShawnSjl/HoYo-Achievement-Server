package tech.sjiale.hoyo_achievement_server.dto;

import lombok.Data;

@Data
public class SrAchievementRecordDto {
    private Integer achievement_id;
    private String className;
    private String name;
    private String description;
    private Integer reward_level;
    private Integer hidden;
    private String game_version;
    private Integer complete;
}

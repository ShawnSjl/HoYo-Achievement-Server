package tech.sjiale.hoyo_achievement_server.dto;

import lombok.Data;

@Data
public class SrAchievementRecordDto {
    private Integer achievementId;
    private String className;
    private String name;
    private String description;
    private Integer rewardLevel;
    private Integer hidden;
    private String gameVersion;
    private Integer complete;
}

package tech.sjiale.hoyo_achievement_server.dto;

import lombok.Data;

@Data
public class ZzzAchievementRecordDto {
    private Integer achievementId;
    private Integer classId;
    private String name;
    private String description;
    private Integer rewardLevel;
    private Integer hidden;
    private String gameVersion;
    private Integer complete;
}

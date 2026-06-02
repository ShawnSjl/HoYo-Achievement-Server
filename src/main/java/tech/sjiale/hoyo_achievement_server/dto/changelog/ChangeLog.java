package tech.sjiale.hoyo_achievement_server.dto.changelog;

import lombok.Data;

@Data
public class ChangeLog {
    private ChangeEntityType entityType;
    private String entityId;
    private ChangeAction action;
}

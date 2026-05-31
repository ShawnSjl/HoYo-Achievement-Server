package tech.sjiale.hoyo_achievement_server.dto;

import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;

@Data
public class ChangeLog {
    private ChangeEntityType entityType;
    private String entityId;
    private ChangeAction action;
}

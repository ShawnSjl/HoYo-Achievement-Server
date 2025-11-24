package tech.sjiale.hoyo_achievement_server.dto;

import java.util.List;

public record MigrationFile (
        String name,
        String type,
        List<String> depends,
        MigrationPayload payload
){
}

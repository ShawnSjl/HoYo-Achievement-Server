package tech.sjiale.hoyo_achievement_server.dto;

import java.util.List;
import java.util.Map;

public record MigrationOperation(
        String action,
        String table,
        List<Map<String, Object>> values
) {
}

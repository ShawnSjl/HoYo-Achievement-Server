package tech.sjiale.hoyo_achievement_server.dto;

import java.util.List;

public record MigrationPayload(
        List<MigrationOperation> operations
) {
}

package tech.sjiale.hoyo_achievement_server.dto;


public record MigrationResult(
        ImportStatus status,
        String fileName,
        String message
) {
    public static MigrationResult success(String fileName) {
        return new MigrationResult(ImportStatus.SUCCESS, fileName, null);
    }

    public static MigrationResult imported(String fileName) {
        return new MigrationResult(ImportStatus.IMPORTED, fileName, null);
    }

    public static MigrationResult failed(String fileName, String message) {
        return new MigrationResult(ImportStatus.FAIL, fileName, message);
    }
}

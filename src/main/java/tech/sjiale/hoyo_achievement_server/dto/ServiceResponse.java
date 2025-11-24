package tech.sjiale.hoyo_achievement_server.dto;

public record ServiceResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ServiceResponse<T> success(String message) {
        return new ServiceResponse<>(true, message, null);
    }

    public static <T> ServiceResponse<T> success(String message, T data) {
        return new ServiceResponse<>(true, message, data);
    }

    public static <T> ServiceResponse<T> error(String message) {
        return new ServiceResponse<>(false, message, null);
    }

    public static <T> ServiceResponse<T> error(String message, T data) {
        return new ServiceResponse<>(false, message, data);
    }
}

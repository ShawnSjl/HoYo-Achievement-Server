package tech.sjiale.hoyo_achievement_server.util;

import cn.dev33.satoken.util.SaResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public SaResult handle(ResponseStatusException ex) {
        return SaResult
                .error(ex.getMessage())
                .setCode(ex.getStatusCode().value());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public SaResult handleMethod(HttpRequestMethodNotSupportedException ex) {
        return SaResult
                .error(ex.getMethod())
                .setCode(ex.getStatusCode().value());
    }
}

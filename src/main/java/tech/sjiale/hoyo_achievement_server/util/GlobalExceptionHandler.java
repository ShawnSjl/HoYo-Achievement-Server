package tech.sjiale.hoyo_achievement_server.util;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public SaResult handle(ResponseStatusException ex) {
        return SaResult
                .error(ex.getMessage())
                .setCode(ex.getStatusCode().value());
    }

    /**
     * Handle HttpRequestMethodNotSupportedException
     *
     * @param ex HttpRequestMethodNotSupportedException
     * @return SaResult
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public SaResult handleMethod(HttpRequestMethodNotSupportedException ex) {
        return SaResult
                .error(ex.getMethod())
                .setCode(ex.getStatusCode().value());
    }

    /**
     * Handle NotLoginException
     *
     * @param nle NotLoginException
     * @return SaResult
     */
    @ExceptionHandler(NotLoginException.class)
    public SaResult handleNotLoginException(NotLoginException nle) {
        log.error("User is not login: {}", nle.getMessage());
        return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Handle NotRoleException
     *
     * @param nre NotRoleException
     * @return SaResult
     */
    @ExceptionHandler(NotRoleException.class)
    public SaResult handleNotRoleException(NotRoleException nre) {
        log.error("User is not admin or root: {}", nre.getMessage());
        return SaResult.error("用户无权限").setCode(HttpStatus.FORBIDDEN.value());
    }
}

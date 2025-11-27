package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tech.sjiale.hoyo_achievement_server.dto.LoginRequest;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.service.UserService;
import tech.sjiale.hoyo_achievement_server.util.ParameterChecker;

@Slf4j
@RestController
@RequestMapping("/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Login
     *
     * @param request login request with username and password
     * @return SaResult with token
     */
    @PostMapping("login")
    public SaResult doLogin(@RequestBody LoginRequest request) {
        log.debug("Login request: {}", request);

        // Check if the username and password are valid
        if (!ParameterChecker.isValidUsername(request.getUsername()) ||
                !ParameterChecker.isValidPassword(request.getPassword())) {
            log.error("Invalid username or password.");
            return SaResult.error("登陆失败").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Check if the user exists
        ServiceResponse<User> userResponse = userService.getUserByName(request.getUsername());
        if (!userResponse.success()) {
            log.error("User {} doesn't exist.", request.getUsername());
            return SaResult.error("用户不存在").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Check if the password matches
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), userResponse.data().getPassword())) {
            log.error("Password doesn't match.");
            return SaResult.error("密码错误").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Login
        log.info("User {} login successfully.", request.getUsername());
        StpUtil.login(userResponse.data().getId());
        return SaResult.ok("登陆成功").setData(StpUtil.getTokenInfo());
    }

    /**
     * Is user login; it will check token in header
     *
     * @return SaResult
     */
    @GetMapping("isLogin")
    public SaResult isLogin() {
        return SaResult.ok("是否登录：" + StpUtil.isLogin()).setData(StpUtil.isLogin());
    }

    /**
     * User logout
     *
     * @return SaResult
     */
    @PostMapping("logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok();
    }
}

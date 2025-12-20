package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.sjiale.hoyo_achievement_server.dto.UserExposeDto;
import tech.sjiale.hoyo_achievement_server.dto.user_request.*;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;
import tech.sjiale.hoyo_achievement_server.service.UserService;
import tech.sjiale.hoyo_achievement_server.util.ParameterChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user/")
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
        // Check if the username and password are valid
        if (ParameterChecker.isUsernameInvalid(request.getUsername()) ||
                ParameterChecker.isPasswordInvalid(request.getPassword())) {
            log.error("Invalid username or password.");
            return SaResult.error("用户名或密码格式错误").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Check if the user exists
        ServiceResponse<User> userResponse = userService.getUserByName(request.getUsername());
        if (!userResponse.success()) {
            log.error(userResponse.message());
            return SaResult.error("用户不存在").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Check if the password matches
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), userResponse.data().getPassword())) {
            log.error("Password doesn't match.");
            return SaResult.error("登录失败").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Login
        StpUtil.login(userResponse.data().getId());

        // Set response data
        Map<String, Object> map = new HashMap<>();
        map.put("token", StpUtil.getTokenInfo().tokenValue);
        map.put("username", userResponse.data().getUsername());
        map.put("isAdmin", userResponse.data().getRole() != UserRole.USER);
        return SaResult.ok("登录成功").setData(map);
    }

    /**
     * Is user login; it will check token in header
     *
     * @return SaResult
     */
    @GetMapping("is-login")
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
        return SaResult.ok("用户已登出");
    }

    /**
     * Check if the user is admin or root
     *
     * @return SaResult
     */
    @GetMapping("is-admin")
    @SaCheckLogin
    public SaResult isAdmin() {
        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the user is admin or root
        ServiceResponse<User> currentUser = userService.getUserById(userId);
        if (!currentUser.success()) {
            log.error(currentUser.message());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, currentUser.message());
        }
        boolean isAdmin = currentUser.data().getRole() != UserRole.USER;

        return SaResult.ok().setData(isAdmin);
    }

    /**
     * Get all users;
     * Should only be called by admin or root
     *
     * @return SaResult with a list of User objects
     */
    @GetMapping("all")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    public SaResult getAllUsers() {
        // Get all users
        ServiceResponse<List<UserExposeDto>> response = userService.getAllUsers();
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取全部用户失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        log.info(response.message());
        return SaResult.ok("获取全部用户成功").setData(response.data());
    }

    /**
     * Create a new user;
     * Should only be called by admin or root
     *
     * @param request CreateRequest with username and password
     * @return SaResult
     */
    @PostMapping("create")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    @SaCheckSafe
    public SaResult createUser(@RequestBody CreateRequest request) {
        // Check if the username and password are valid
        if (ParameterChecker.isUsernameInvalid(request.getUsername()) ||
                ParameterChecker.isPasswordInvalid(request.getPassword())) {
            log.error("Invalid username or password for new user.");
            return SaResult.error("用户名或密码格式错误").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Create a new user
        ServiceResponse<?> response = userService.createUser(request.getUsername(), request.getPassword());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("用户名已存在").setCode(HttpStatus.BAD_REQUEST.value());
        }
        log.info(response.message());
        return SaResult.ok("新用户创建成功");
    }

    /**
     * Update username
     *
     * @param request UpdateUsernameRequest with a new username
     * @return SaResult
     */
    @PutMapping("update-username")
    @SaCheckLogin
    @SaCheckSafe
    public SaResult updateUsername(@RequestBody UpdateUsernameRequest request) {
        // Check if the username is valid
        if (ParameterChecker.isUsernameInvalid(request.getUsername())) {
            log.error("Invalid new username: {}", request.getUsername());
            return SaResult.error("新用户名格式错误").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Update username
        ServiceResponse<?> response = userService.updateUsername(userId, request.getUsername());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("用户名已存在").setCode(HttpStatus.BAD_REQUEST.value());
        }
        log.info(response.message());
        return SaResult.ok("用户名更新成功");
    }

    /**
     * Update password
     *
     * @param request UpdatePasswordRequest with a new password
     * @return SaResult
     */
    @PutMapping("update-password")
    @SaCheckLogin
    @SaCheckSafe
    public SaResult updatePassword(@RequestBody UpdatePasswordRequest request) {
        // Check if the password is valid
        if (ParameterChecker.isPasswordInvalid(request.getPassword())) {
            log.error("Invalid new password: {}", request.getPassword());
            return SaResult.error("新密码格式错误").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Update password
        ServiceResponse<?> response = userService.updatePassword(userId, request.getPassword());
        log.info(response.message());
        return SaResult.ok("密码更新成功");
    }

    /**
     * Update user status;
     * Should only be called by admin or root
     *
     * @param request UpdateStatusRequest with user id and status
     * @return SaResult
     */
    @PutMapping("update-status")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    @SaCheckSafe
    public SaResult updateUserStatus(@RequestBody UpdateStatusRequest request) {
        // Update user status
        ServiceResponse<?> response = userService.updateUserStatus(request.getUserId(), request.getStatus());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("状态更新错误").setCode(HttpStatus.BAD_REQUEST.value());
        }
        log.info(response.message());
        return SaResult.ok("状态更新成功");
    }

    /**
     * Update user role;
     * Should only be called by admin or root
     *
     * @param request UpdateRoleRequest with user id and role
     * @return SaResult
     */
    @PutMapping("update-role")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    @SaCheckSafe
    public SaResult updateUserRole(@RequestBody UpdateRoleRequest request) {
        // Update user role
        ServiceResponse<?> response = userService.updateUserRole(request.getUserId(), request.getRole());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("权限更新错误").setCode(HttpStatus.BAD_REQUEST.value());
        }
        log.info(response.message());
        return SaResult.ok("用户权限更新成功");
    }

    /**
     * Delete user;
     * Should only be called by the user itself
     *
     * @return SaResult
     */
    @DeleteMapping("delete")
    @SaCheckLogin
    @SaCheckSafe
    public SaResult deleteUser() {
        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Delete user
        ServiceResponse<?> response = userService.deleteUser(userId);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("Root账户无法删除").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Logout
        StpUtil.logout();

        log.info(response.message());
        return SaResult.ok("用户删除成功");
    }

    @PostMapping("second-auth")
    @SaCheckLogin
    public SaResult secondAuth(@RequestBody SecondAuthRequest request) {
        // Check if the password is valid
        if (ParameterChecker.isPasswordInvalid(request.getPassword())) {
            log.error("Invalid password.");
            return SaResult.error("密码格式错误").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Get current user
        ServiceResponse<User> userResponse = userService.getUserById(userId);
        if (!userResponse.success()) {
            log.error(userResponse.message());
            return SaResult.error("用户不存在").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Check if the password matches
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), userResponse.data().getPassword())) {
            log.error("Password doesn't match.");
            return SaResult.error("二级验证失败").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Open safe for 2 minutes
        StpUtil.openSafe(120);
        return SaResult.ok("二级验证成功");
    }
}

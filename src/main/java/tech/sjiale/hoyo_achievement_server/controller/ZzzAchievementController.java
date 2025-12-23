package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.sjiale.hoyo_achievement_server.dto.*;
import tech.sjiale.hoyo_achievement_server.dto.achievement_request.UpdateRecordRequest;
import tech.sjiale.hoyo_achievement_server.entity.*;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;
import tech.sjiale.hoyo_achievement_server.service.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/zzz")
@RequiredArgsConstructor
public class ZzzAchievementController {

    private final AccountService accountService;
    private final UserService userService;
    private final ZzzUserRecordService zzzUserRecordService;
    private final ZzzAchievementService zzzAchievementService;
    private final ZzzBranchService zzzBranchService;

    /**
     * Get all ZZZ achievements
     *
     * @return SaResult
     */
    @GetMapping("all")
    public SaResult getAllAchievements() {
        ServiceResponse<List<ZzzAchievement>> response = zzzAchievementService.getAllAchievements();
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("ZZZ成就列表获取失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("ZZZ成就列表获取成功").setData(response.data());
    }

    @GetMapping("account-records")
    @SaCheckLogin
    public SaResult getAccountRecords(@RequestParam String uuid) {
        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the user is disabled
        if (isUserDisabled(userId)) {
            return SaResult.error("用户已被禁用").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Check if the account uuid belongs to the user
        if (isUserNotOwnAccount(userId, uuid)) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        ServiceResponse<List<ZzzUserRecord>> response = zzzUserRecordService.getAllRecordByUUID(uuid);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("账号ZZZ成就记录获取失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("账号ZZZ成就记录获取成功").setData(response.data());
    }

    /**
     * Update achievement by id
     *
     * @param request UpdateRecordRequest with achievement id and record status
     * @return SaResult
     */
    @PutMapping("update")
    @SaCheckLogin
    public SaResult updateAchievementById(@RequestBody UpdateRecordRequest request) {
        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the user is disabled
        if (isUserDisabled(userId)) {
            return SaResult.error("用户已被禁用").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Check if the account uuid belongs to the user
        if (isUserNotOwnAccount(userId, request.getUuid())) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Check if the complete status is valid
        if (request.getCompleteStatus() < 0 || request.getCompleteStatus() > 1) {
            return SaResult.error("更新状态非法").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Update record
        ServiceResponse<?> response = zzzUserRecordService.updateRecordById(request.getUuid(),
                request.getAchievementId(), request.getCompleteStatus());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("成就更新失败").setCode(HttpStatus.BAD_REQUEST.value());
        }
        return SaResult.ok("成就更新状态成功");
    }

    /**
     * Get all branches
     *
     * @return SaResult
     */
    @GetMapping("branches")
    public SaResult getAllBranches() {
        ServiceResponse<List<ZzzBranch>> response = zzzBranchService.getAllBranches();
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取ZZZ成就分支列表失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("获取ZZZ成就分支列表成功").setData(response.data());
    }

    /**
     * Helper method to check if the user doesn't own the account
     *
     * @param userId      user id
     * @param accountUuid account uuid
     * @return true if the user doesn't own the account, false otherwise
     */
    private boolean isUserNotOwnAccount(Long userId, String accountUuid) {
        // Get all accounts by user id
        ServiceResponse<List<Account>> response = accountService.getAllAccountsByUserId(userId);
        if (!response.success()) {
            log.error(response.message());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, response.message());
        }

        // Check if the account uuid belongs to the user
        boolean ownUuid = false;
        for (Account account : response.data()) {
            if (account.getAccountUuid().equals(accountUuid)) {
                ownUuid = true;
                break;
            }
        }
        if (!ownUuid) {
            log.error("User {} doesn't own account {}.", userId, accountUuid);
        }
        return !ownUuid;
    }

    /**
     * Helper method to check if the user is disabled
     *
     * @param userId user id
     * @return true if disabled, false otherwise
     */
    private boolean isUserDisabled(Long userId) {
        // Get user info
        ServiceResponse<User> userResponse = userService.getUserById(userId);
        if (!userResponse.success()) {
            log.error(userResponse.message());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, userResponse.message());
        }

        // Check if the user is disabled
        User user = userResponse.data();
        if (user.getStatus() == UserStatus.DISABLED) {
            log.error("User {} is disabled.", userId);
            return true;
        }
        return false;
    }
}

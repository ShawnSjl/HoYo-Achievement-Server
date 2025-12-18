package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.sjiale.hoyo_achievement_server.dto.achievement_request.AllUserRecordRequest;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.SrAchievementRecordDto;
import tech.sjiale.hoyo_achievement_server.dto.achievement_request.UpdateRecordRequest;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.entity.SrBranch;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;
import tech.sjiale.hoyo_achievement_server.service.AccountService;
import tech.sjiale.hoyo_achievement_server.service.SrBranchService;
import tech.sjiale.hoyo_achievement_server.service.SrUserRecordService;
import tech.sjiale.hoyo_achievement_server.service.UserService;
import tech.sjiale.hoyo_achievement_server.util.AuthUtil;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sr")
@RequiredArgsConstructor
public class SrAchievementController {

    private final AccountService accountService;
    private final UserService userService;
    private final SrUserRecordService srUserRecordService;
    private final SrBranchService srBranchService;

    @GetMapping("all")
    public SaResult getAllAchievements(@RequestBody AllUserRecordRequest request) {
        // Check if the user is login
        if (AuthUtil.isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

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

        ServiceResponse<List<SrAchievementRecordDto>> response = srUserRecordService.getAllAchievementsRecordByUUID(request.getUuid());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取用户SR成就列表失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("获取用户成就列表成功").setData(response.data());
    }

    /**
     * Get all achievements with an empty record list
     *
     * @return SaResult
     */
    @GetMapping("all-empty-record")
    public SaResult getAllAchievementsWithEmptyRecord() {
        ServiceResponse<List<SrAchievementRecordDto>> response = srUserRecordService.getAllAchievementsEmptyRecord();
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取SR成就列表失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("获取SR成就列表成功").setData(response.data());
    }

    /**
     * Update achievement by id
     *
     * @param request UpdateRecordRequest with achievement id and record status
     * @return SaResult
     */
    @PutMapping("update")
    public SaResult updateAchievementById(@RequestBody UpdateRecordRequest request) {
        // Check if the user is login
        if (AuthUtil.isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

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
        ServiceResponse<?> response = srUserRecordService.updateRecordById(request.getUuid(),
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
        ServiceResponse<List<SrBranch>> response = srBranchService.getAllBranches();
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取SR成就分支列表失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("获取SR成就分支列表成功").setData(response.data());
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

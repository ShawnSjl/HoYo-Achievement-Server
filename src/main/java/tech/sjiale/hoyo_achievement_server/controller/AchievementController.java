package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.achievement_request.UpdateRecordRequest;
import tech.sjiale.hoyo_achievement_server.entity.*;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;
import tech.sjiale.hoyo_achievement_server.service.*;
import tech.sjiale.hoyo_achievement_server.util.ParameterChecker;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AccountService accountService;
    private final UserService userService;
    private final AchievementService achievementService;
    private final UserRecordService userRecordService;
    private final DataChangeLogService dataChangeLogService;

    /**
     * Get all achievements by game id
     *
     * @param gameId game id
     * @return SaResult
     */
    @GetMapping("all")
    public SaResult getAllAchievementByGameId(@RequestParam GameId gameId) {
        ServiceResponse<List<Achievement>> response = achievementService.getAllAchievementsByGameId(gameId);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error(gameId + "成就列表获取失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok(gameId + "成就列表获取成功").setData(response.data());
    }

    /**
     * Get all branches by game id
     *
     * @param gameId game id
     * @return SaResult
     */
    @GetMapping("branches")
    public SaResult getAllBranchByGameId(@RequestParam GameId gameId) {
        ServiceResponse<List<Achievement>> response = achievementService.getAllBranchesByGameId(gameId);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error(gameId + "成就分支列表获取失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok(gameId + "成就分支列表获取成功").setData(response.data());
    }

    /**
     * Get all records of an account
     *
     * @param uuid account uuid
     * @return SaResult
     */
    @GetMapping("account-records")
    @SaCheckLogin
    public SaResult getRecordsById(@RequestParam String uuid) {
        // Validate input
        if (ParameterChecker.isAccountUuidInvalid(uuid)) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the user is disabled
        ServiceResponse<Boolean> userResponse = userService.isUserDisabled(userId);
        if (!userResponse.success()) {
            log.error(userResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        if (userResponse.data()) {
            return SaResult.error("用户已被禁用").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Check if the account uuid belongs to the user
        ServiceResponse<Boolean> accountResponse = accountService.isUserOwnAccount(userId, uuid);
        if (!accountResponse.success()) {
            log.warn(accountResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        if (!accountResponse.data()) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        ServiceResponse<List<UserRecord>> response = userRecordService.getAllRecordByUUID(uuid);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("账号成就记录获取失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return SaResult.ok("账号成就记录获取成功").setData(response.data());
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
        // Validate input
        if (ParameterChecker.isAccountUuidInvalid(request.getUuid())) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Check if the complete status is valid
        if (request.getCompleteStatus() < 0 || request.getCompleteStatus() > 1) {
            return SaResult.error("更新状态非法").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the user is disabled
        ServiceResponse<Boolean> userResponse = userService.isUserDisabled(userId);
        if (!userResponse.success()) {
            log.error(userResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        if (userResponse.data()) {
            return SaResult.error("用户已被禁用").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Check if the account uuid belongs to the user
        ServiceResponse<Boolean> accountResponse = accountService.isUserOwnAccount(userId, request.getUuid());
        if (!accountResponse.success()) {
            log.warn(accountResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        if (!accountResponse.data()) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Update record
        ServiceResponse<?> response = userRecordService.updateRecordById(request.getUuid(), request.getGameId(),
                request.getAchievementId(), request.getCompleteStatus());
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("成就更新失败").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Add change log
        dataChangeLogService.addChangeLog(userId, ChangeEntityType.ACCOUNT_RECORD, request.getUuid(), ChangeAction.UPDATE);
        return SaResult.ok("成就更新状态成功");
    }
}

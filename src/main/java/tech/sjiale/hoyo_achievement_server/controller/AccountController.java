package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountCreateRequest;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountUpdateNameRequest;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountUpdateUidRequest;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountDeleteRequest;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.service.AccountService;
import tech.sjiale.hoyo_achievement_server.service.UserService;
import tech.sjiale.hoyo_achievement_server.util.ParameterChecker;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    /**
     * Get all accounts by user id;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @return SaResult
     */
    @GetMapping("/get-by-user-id")
    @SaCheckLogin
    public SaResult getAccountByUserId() {
        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Get all accounts by user id
        ServiceResponse<List<Account>> response = accountService.getAllAccountsByUserId(userId);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取用户账号失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        log.info(response.message());
        return SaResult.ok("获取当前用户账号成功").setData(response.data());
    }

    /**
     * Get an account by account uuid; Should only be called by the user itself; Should be called after user login,
     * satoken will authenticate the user
     *
     * @param accountUuid account uuid
     * @return SaResult
     */
    @GetMapping("/get-by-uuid")
    @SaCheckLogin
    public SaResult getAccountByUuid(@RequestParam String accountUuid) {
        // Valid
        if (ParameterChecker.isAccountUuidInvalid(accountUuid)) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Get account by uuid
        ServiceResponse<Account> response = accountService.getAccountByUuid(accountUuid);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("获取用户账号失败").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Check if the account belongs to the user
        if (response.data().getUserId() != StpUtil.getLoginIdAsLong()) {
            log.warn("User {} tried to get account {} that not belong to it.", StpUtil.getLoginIdAsLong(), accountUuid);
            return SaResult.error("获取用户账号失败").setCode(HttpStatus.BAD_REQUEST.value());
        }

        log.info("{} UUID: {}", response.message(), accountUuid);
        return SaResult.ok("获取指定用户账号成功").setData(response.data());
    }

    /**
     * Create a new account;
     * One user could have maximum 10 accounts;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param clientId client id, used to identify the source of the request
     * @param account  Account entity
     * @return SaResult
     */
    @PostMapping("/create")
    @SaCheckLogin
    public SaResult createAccount(@RequestParam String clientId, @RequestBody AccountCreateRequest account) {
        // Valid
        if (ParameterChecker.isAccountUuidInvalid(account.getAccountUuid())
                || ParameterChecker.isAccountNameInvalid(account.getAccountName())
                || ParameterChecker.isAccountInGameUidInvalid(account.getAccountInGameUid())) {
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

        // Check if the user already has 10 accounts
        ServiceResponse<List<Account>> allAccountsResponse = accountService.getAllAccountsByUserId(userId);
        if (!allAccountsResponse.success()) {
            log.error(allAccountsResponse.message());
            return SaResult.error("获取已有账号错误").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        if (allAccountsResponse.data().size() >= 10) {
            log.warn("User {} already has 10 accounts.", userId);
            return SaResult.error("账号数量达到上限").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Create an account instance
        Account newAccount = new Account();
        newAccount.setAccountUuid(account.getAccountUuid());
        newAccount.setUserId(userId);
        newAccount.setGameId(account.getGameId());
        newAccount.setAccountName(account.getAccountName());
        newAccount.setAccountInGameUid(account.getAccountInGameUid());

        // Create that account
        ServiceResponse<?> response = accountService.createAccount(userId, clientId, newAccount);
        if (!response.success()) {
            log.error(response.message());
            return SaResult.error("创建用户失败").setCode(HttpStatus.BAD_REQUEST.value());
        }
        log.info(response.message());
        return SaResult.ok("创建用户成功");
    }

    /**
     * Update account name by account uuid;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param clientId client id, used to identify the source of the request
     * @param req      AccountUpdateNameRequest
     * @return SaResult
     */
    @PutMapping("/update-name")
    @SaCheckLogin
    public SaResult updateAccountName(@RequestParam String clientId, @RequestBody AccountUpdateNameRequest req) {
        // Validate input
        if (ParameterChecker.isAccountUuidInvalid(req.getAccountUuid())
                || ParameterChecker.isAccountNameInvalid(req.getAccountName())) {
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
        ServiceResponse<Boolean> accountResponse = accountService.isUserOwnAccount(userId, req.getAccountUuid());
        if (!accountResponse.success()) {
            log.warn(accountResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.BAD_REQUEST.value());
        }
        if (!accountResponse.data()) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Update account name
        ServiceResponse<?> response = accountService.updateAccountName(userId, clientId, req.getAccountUuid(), req.getAccountName()
        );
        log.info(response.message());

        return SaResult.ok("游戏账户名称更新成功");
    }

    /**
     * Update account in game uid by account uuid;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param clientId client id, used to identify the source of the request
     * @param req      AccountUpdateUidRequest
     * @return SaResult
     */
    @PutMapping("/update-in-game-uid")
    @SaCheckLogin
    public SaResult updateAccountInGameUid(@RequestParam String clientId, @RequestBody AccountUpdateUidRequest req) {
        // Validate input
        if (ParameterChecker.isAccountUuidInvalid(req.getAccountUuid())
                || ParameterChecker.isAccountInGameUidInvalid(req.getAccountInGameUid())) {
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
        ServiceResponse<Boolean> accountResponse = accountService.isUserOwnAccount(userId, req.getAccountUuid());
        if (!accountResponse.success()) {
            log.warn(accountResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.BAD_REQUEST.value());
        }
        if (!accountResponse.data()) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Update account in game uid
        ServiceResponse<?> response = accountService.updateAccountInGameUid(userId, clientId, req.getAccountUuid(), req.getAccountInGameUid()
        );
        log.info(response.message());

        return SaResult.ok("游戏账户uid更新成功");
    }

    /**
     * Delete an account by account uuid;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param clientId client id, used to identify the source of the request
     * @param req      AccountDeleteRequest
     * @return SaResult
     */
    @DeleteMapping("/delete")
    @SaCheckLogin
    @SaCheckSafe
    public SaResult deleteAccount(@RequestParam String clientId, @RequestBody AccountDeleteRequest req) {
        // Validate input
        if (ParameterChecker.isAccountUuidInvalid(req.getAccountUuid())) {
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
        ServiceResponse<Boolean> accountResponse = accountService.isUserOwnAccount(userId, req.getAccountUuid());
        if (!accountResponse.success()) {
            log.warn(accountResponse.message());
            return SaResult.error("未找到对应用户").setCode(HttpStatus.BAD_REQUEST.value());
        }
        if (!accountResponse.data()) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.FORBIDDEN.value());
        }

        // Delete that account
        ServiceResponse<?> response = accountService.deleteAccount(userId, clientId, req.getAccountUuid());
        log.info(response.message());

        return SaResult.ok("删除账户成功");
    }
}

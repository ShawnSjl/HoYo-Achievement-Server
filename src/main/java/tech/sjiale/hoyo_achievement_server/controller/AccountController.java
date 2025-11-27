package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountUpdateNameRequest;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountUpdateUidRequest;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.account_request.AccountDeleteRequest;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.service.AccountService;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Get all accounts by user id;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @return SaResult
     */
    @GetMapping("/get-by-user-id")
    public SaResult getAccountByUserId() {
        // Check if the user is login
        if (isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Get all accounts by user id
        ServiceResponse<List<Account>> response = accountService.getAllAccountsByUserId(userId);
        if (!response.success()) {
            log.error("Get all accounts by user id failed: {}", response.message());
            return SaResult.error(response.message()).setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        log.info("Get all accounts by user id {} successfully.", userId);
        return SaResult.ok().setData(response.data());
    }

    /**
     * Create a new account;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param account Account entity
     * @return SaResult
     */
    @PutMapping("/create")
    public SaResult createAccount(@RequestBody Account account) {
        // Check if the user is login
        if (isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Valid
        if (!Objects.equals(account.getUserId(), userId)) {
            log.error("Account user id {} doesn't match request user id {}.", account.getUserId(), userId);
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        } else if (isAccountUuidInvalid(account.getAccountUuid())
                || isAccountNameInvalid(account.getAccountName())
                || isAccountInGameUidInvalid(account.getAccountInGameUid())) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Create that account
        accountService.createAccount(account);
        log.info("Create account {} successfully.", account.getAccountUuid());
        return SaResult.ok("创建用户成功");
    }

    /**
     * Update account name by account uuid;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param req AccountUpdateNameRequest
     * @return SaResult
     */
    @PutMapping("/update-name")
    public SaResult updateAccountName(@RequestBody AccountUpdateNameRequest req) {
        // Validate input
        if (isAccountUuidInvalid(req.getAccountUuid())
                || isAccountNameInvalid(req.getAccountName())) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Check if the user is login
        if (isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the account uuid belongs to the user
        if (isUserNotOwnAccount(userId, req.getAccountUuid())) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Update account name
        accountService.updateAccountName(req.getAccountUuid(), req.getAccountName());
        log.info("Update account name {} successfully.", req.getAccountUuid());
        return SaResult.ok("游戏账户名称更新成功");
    }

    /**
     * Update account in game uid by account uuid;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param req AccountUpdateUidRequest
     * @return SaResult
     */
    @PutMapping("/update-in-game-uid")
    public SaResult updateAccountInGameUid(@RequestBody AccountUpdateUidRequest req) {
        // Validate input
        if (isAccountUuidInvalid(req.getAccountUuid())
                || isAccountInGameUidInvalid(req.getAccountInGameUid())) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Check if the user is login
        if (isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the account uuid belongs to the user
        if (isUserNotOwnAccount(userId, req.getAccountUuid())) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Update account in game uid
        accountService.updateAccountInGameUid(req.getAccountUuid(), req.getAccountInGameUid());
        log.info("Update account in game uid {} successfully.", req.getAccountUuid());
        return SaResult.ok("游戏账户uid更新成功");
    }

    /**
     * Delete an account by account uuid;
     * Should only be called by the user itself;
     * Should be called after user login, satoken will authenticate the user
     *
     * @param req AccountDeleteRequest
     * @return SaResult
     */
    @DeleteMapping("/delete")
    public SaResult deleteAccount(@RequestBody AccountDeleteRequest req) {
        // Validate input
        if (isAccountUuidInvalid(req.getAccountUuid())) {
            return SaResult.error("错误请求内容").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Check if the user is login
        if (isNotLogin()) {
            return SaResult.error("用户未登录").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Get user id from token
        Long userId = StpUtil.getLoginIdAsLong();

        // Check if the account uuid belongs to the user
        if (isUserNotOwnAccount(userId, req.getAccountUuid())) {
            return SaResult.error("非对应用户请求").setCode(HttpStatus.UNAUTHORIZED.value());
        }

        // Delete that account
        accountService.deleteAccount(req.getAccountUuid());
        log.info("Delete account successfully.");
        return SaResult.ok("删除账户成功");
    }

    /**
     * Helper method to check if the user is login
     *
     * @return true if not login, false otherwise
     */
    private boolean isNotLogin() {
        boolean login = StpUtil.isLogin();
        if (!login) {
            log.error("User is not login.");
        }
        return !login;
    }

    /**
     * Helper method to check if the account uuid is invalid
     *
     * @param accountUuid account uuid
     * @return true if invalid, false otherwise
     */
    private boolean isAccountUuidInvalid(String accountUuid) {
        boolean invalid = accountUuid == null
                || accountUuid.isBlank()
                || accountUuid.length() > 512;
        if (invalid) {
            log.error("Account uuid is invalid: {}.", accountUuid);
        }
        return invalid;
    }

    /**
     * Helper method to check if the account name is invalid
     *
     * @param accountName account name
     * @return true if invalid, false otherwise
     */
    private boolean isAccountNameInvalid(String accountName) {
        boolean invalid = accountName == null
                || accountName.isBlank()
                || accountName.length() > 24;
        if (invalid) {
            log.error("Account name is invalid: {}.", accountName);
        }
        return invalid;
    }

    /**
     * Helper method to check if the account in game uid is invalid
     *
     * @param accountInGameUid account in game uid
     * @return true if invalid, false otherwise
     */
    private boolean isAccountInGameUidInvalid(String accountInGameUid) {
        if (accountInGameUid == null) return true;

        boolean invalid = accountInGameUid.length() > 128;
        if (invalid) {
            log.error("Account in game uid is too long.");
        }
        return invalid;
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
            log.error("Get all accounts by user id failed: {}", response.message());
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
}

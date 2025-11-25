package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameType;
import tech.sjiale.hoyo_achievement_server.mapper.AccountMapper;

import java.util.List;

@Slf4j
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    /**
     * Get account by uuid
     */
    public ServiceResponse<Account> getAccountByUuid(String uuid) {
        Account account = this.lambdaQuery()
                .eq(Account::getAccount_uuid, uuid)
                .one();

        if (account == null) {
            log.error("Account not found for uuid: {}.", uuid);
            return ServiceResponse.error("Account not found.");
        }
        log.debug("Get account by uuid successfully.");
        return ServiceResponse.success("Get account by uuid successfully.", account);
    }

    /**
     * Get all accounts by user id
     */
    public ServiceResponse<List<Account>> getAllAccountsByUserId(Long userId) {
        List<Account> accounts = this.lambdaQuery().eq(Account::getUser_id, userId).list();

        if (accounts == null || accounts.isEmpty()) {
            log.error("No account found for user id: {}.", userId);
            return ServiceResponse.error("No account found.");
        }
        log.debug("Get all accounts by user id successfully.");
        return ServiceResponse.success("Get all accounts by user id successfully.", accounts);
    }

    /**
     * Create new account
     *
     * @param userId           user id
     * @param uuid             account uuid
     * @param gameType         account game type
     * @param accountName      account name
     * @param accountInGameUid account in game uid
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> createAccount(Long userId, String uuid, GameType gameType, String accountName,
                                            String accountInGameUid) {
        // Check uniqueness of uuid
        ServiceResponse<Account> response = getAccountByUuid(uuid);
        if (response.success()) {
            log.error("Account already exists for uuid: {}.", uuid);
            throw new IllegalArgumentException("Account already exists.");
        }

        // Create new account
        Account account = new Account();
        account.setAccount_uuid(uuid);
        account.setUser_id(userId);
        account.setGame_type(gameType);
        account.setAccount_name(accountName);
        if (accountInGameUid != null) {
            account.setAccount_in_game_uid(accountInGameUid);
        }

        // Save new account
        boolean success = this.save(account);
        if (success) {
            log.debug("Create account successfully.");
            return ServiceResponse.success("Create account successfully.");
        } else {
            log.error("Create account failed.");
            throw new RuntimeException("Create account failed.");
        }
    }

    /**
     * Update account name
     */
    @Transactional
    public ServiceResponse<?> updateAccountName(String uuid, String newName) {
        // Update account name
        boolean updated = this.lambdaUpdate()
                .eq(Account::getAccount_uuid, uuid)
                .set(Account::getAccount_name, newName)
                .update();
        if (updated) {
            log.debug("Update account name successfully.");
            return ServiceResponse.success("Update account name successfully.");
        } else {
            log.error("Update account name failed.");
            throw new RuntimeException("Update account name failed.");
        }
    }

    /**
     * Update account in game uid
     *
     * @param uuid account uuid
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateAccountInGameUid(String uuid, String newInGameUid) {
        // Update account in game uid
        boolean updated = this.lambdaUpdate()
                .eq(Account::getAccount_uuid, uuid)
                .set(Account::getAccount_in_game_uid, newInGameUid)
                .update();
        if (updated) {
            log.debug("Update account in game uid successfully.");
            return ServiceResponse.success("Update account in game uid successfully.");
        } else {
            log.error("Update account in game uid failed.");
            throw new RuntimeException("Update account in game uid failed.");
        }
    }

    /**
     * Delete account by uuid
     *
     * @param uuid account uuid
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> deleteAccount(String uuid) {
        // Delete account
        boolean removed = this.lambdaUpdate()
                .eq(Account::getAccount_uuid, uuid)
                .remove();
        if (removed) {
            log.debug("Delete account successfully.");
            return ServiceResponse.success("Delete account successfully.");
        } else {
            log.error("Delete account failed.");
            throw new RuntimeException("Delete account failed.");
        }
    }
}

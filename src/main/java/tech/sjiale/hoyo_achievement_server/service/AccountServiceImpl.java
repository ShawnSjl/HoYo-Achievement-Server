package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.mapper.AccountMapper;

import java.util.List;

@Slf4j
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    /**
     * Get account by uuid
     *
     * @param uuid account uuid
     * @return ServiceResponse with an Account object
     */
    public ServiceResponse<Account> getAccountByUuid(String uuid) {
        Account account = this.lambdaQuery()
                .eq(Account::getAccountUuid, uuid)
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
     *
     * @param userId user id
     * @return ServiceResponse with a list of Account objects
     */
    public ServiceResponse<List<Account>> getAllAccountsByUserId(Long userId) {
        List<Account> accounts = this.lambdaQuery().eq(Account::getUserId, userId).list();

        if (accounts == null) {
            log.error("Get all accounts by user id failed.");
        } else if (accounts.isEmpty()) {
            log.warn("No account found for user id: {}.", userId);
            return ServiceResponse.success("No account found.", accounts);
        }
        log.debug("Get all accounts by user id successfully.");
        return ServiceResponse.success("Get all accounts by user id successfully.", accounts);
    }

    /**
     * Create a new account; should only be called by the user itself
     *
     * @param account Account entity
     */
    @Transactional
    public void createAccount(Account account) {
        // Check the uniqueness of uuid
        ServiceResponse<Account> response = getAccountByUuid(account.getAccountUuid());
        if (response.success()) {
            log.error("Account already exists for uuid: {}.", account.getAccountUuid());
            // TODO: how to handle this exception? now it expose to the client
            throw new IllegalArgumentException("Account already exists.");
        }

        // Save the new account
        boolean success = this.save(account);
        if (!success) {
            log.error("Create account failed.");
            throw new RuntimeException("Create account failed.");
        }
    }

    /**
     * Update account name; should only be called by the user itself
     *
     * @param uuid    account uuid
     * @param newName new account name
     */
    @Transactional
    public void updateAccountName(String uuid, String newName) {
        // Update account name
        boolean updated = this.lambdaUpdate()
                .eq(Account::getAccountUuid, uuid)
                .set(Account::getAccountName, newName)
                .update();
        if (!updated) {
            log.error("Update account name failed.");
            throw new RuntimeException("Update account name failed.");
        }
    }

    /**
     * Update account in game uid; should only be called by the user itself
     *
     * @param uuid account uuid
     */
    @Transactional
    public void updateAccountInGameUid(String uuid, String newInGameUid) {
        // Update account in game uid
        boolean updated = this.lambdaUpdate()
                .eq(Account::getAccountUuid, uuid)
                .set(Account::getAccountInGameUid, newInGameUid)
                .update();
        if (!updated) {
            log.error("Update account in game uid failed.");
            throw new RuntimeException("Update account in game uid failed.");
        }
    }

    /**
     * Delete an account by account uuid; should only be called by the user itself
     *
     * @param uuid account uuid
     */
    @Transactional
    public void deleteAccount(String uuid) {
        // Delete an account
        boolean removed = this.lambdaUpdate()
                .eq(Account::getAccountUuid, uuid)
                .remove();
        if (!removed) {
            log.error("Delete account failed.");
            throw new RuntimeException("Delete account failed.");
        }
    }
}

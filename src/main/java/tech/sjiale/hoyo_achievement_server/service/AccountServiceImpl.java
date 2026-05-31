package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ChangeLog;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;
import tech.sjiale.hoyo_achievement_server.mapper.AccountMapper;

import java.util.List;

@Slf4j
@Service("accountService")
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    private final SseServiceImpl sseService;

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
            return ServiceResponse.error("Get all accounts by user id failed.");
        } else if (accounts.isEmpty()) {
            return ServiceResponse.success("No account found for user id: " + userId, accounts);
        }
        return ServiceResponse.success("Get all accounts successfully for user id: " + userId, accounts);
    }

    /**
     * Create a new account; should only be called by the user itself
     *
     * @param account  Account entity
     * @param userId   user id
     * @param clientId client id
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> createAccount(Account account, Long userId, String clientId) {
        // Check the uniqueness of uuid
        ServiceResponse<Account> response = getAccountByUuid(account.getAccountUuid());
        if (response.success()) {
            return ServiceResponse.error("Account already exists for uuid: " + account.getAccountUuid());
        }

        // Save the new account
        boolean success = this.save(account);
        if (!success) {
            log.error("Create account failed.");
            throw new RuntimeException("Create account failed.");
        }

        // Create changelog and broadcast it
        ChangeLog changeLog = new ChangeLog();
        changeLog.setEntityType(ChangeEntityType.ACCOUNT);
        changeLog.setEntityId(account.getAccountUuid());
        changeLog.setAction(ChangeAction.INSERT);
        sseService.broadcastUpdate(userId, clientId, changeLog);

        return ServiceResponse.success("Create account successfully for uuid: " + account.getAccountUuid());
    }

    /**
     * Update account name; should only be called by the user itself
     *
     * @param uuid     account uuid
     * @param newName  new account name
     * @param userId   user id
     * @param clientId client id
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateAccountName(String uuid, String newName, Long userId, String clientId) {
        // Update account name
        boolean updated = this.lambdaUpdate()
                .eq(Account::getAccountUuid, uuid)
                .set(Account::getAccountName, newName)
                .update();
        if (!updated) {
            log.error("Update account name failed.");
            throw new RuntimeException("Update account name failed.");
        }

        // Create changelog and broadcast it
        ChangeLog changeLog = new ChangeLog();
        changeLog.setEntityType(ChangeEntityType.ACCOUNT);
        changeLog.setEntityId(uuid);
        changeLog.setAction(ChangeAction.UPDATE);
        sseService.broadcastUpdate(userId, clientId, changeLog);

        return ServiceResponse.success("Update account name successfully for uuid: " + uuid);
    }

    /**
     * Update account in game uid; should only be called by the user itself
     *
     * @param uuid     account uuid
     * @param userId   user id
     * @param clientId client id
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateAccountInGameUid(String uuid, String newInGameUid, Long userId, String clientId) {
        // Update account in game uid
        boolean updated = this.lambdaUpdate()
                .eq(Account::getAccountUuid, uuid)
                .set(Account::getAccountInGameUid, newInGameUid)
                .update();
        if (!updated) {
            log.error("Update account in game uid failed.");
            throw new RuntimeException("Update account in game uid failed.");
        }

        // Create changelog and broadcast it
        ChangeLog changeLog = new ChangeLog();
        changeLog.setEntityType(ChangeEntityType.ACCOUNT);
        changeLog.setEntityId(uuid);
        changeLog.setAction(ChangeAction.UPDATE);
        sseService.broadcastUpdate(userId, clientId, changeLog);

        return ServiceResponse.success("Update account in game uid successfully for uuid: " + uuid);
    }

    /**
     * Delete an account by account uuid; should only be called by the user itself
     *
     * @param uuid     account uuid
     * @param userId   user id
     * @param clientId client id
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> deleteAccount(String uuid, Long userId, String clientId) {
        // Delete an account
        boolean removed = this.lambdaUpdate()
                .eq(Account::getAccountUuid, uuid)
                .remove();
        if (!removed) {
            log.error("Delete account failed.");
            throw new RuntimeException("Delete account failed.");
        }

        // Create changelog and broadcast it
        ChangeLog changeLog = new ChangeLog();
        changeLog.setEntityType(ChangeEntityType.ACCOUNT);
        changeLog.setEntityId(uuid);
        changeLog.setAction(ChangeAction.DELETE);
        sseService.broadcastUpdate(userId, clientId, changeLog);

        return ServiceResponse.success("Delete account successfully for uuid: " + uuid);
    }

    /**
     * Check if the user owns the account
     *
     * @param userId user id
     * @param uuid   account uuid
     * @return ServiceResponse with a boolean value
     */
    @Override
    public ServiceResponse<Boolean> isUserOwnAccount(Long userId, String uuid) {
        // Get all accounts by user id
        List<Account> accounts = this.lambdaQuery().eq(Account::getUserId, userId).list();

        if (accounts == null) {
            return ServiceResponse.error("Get all accounts by user id failed.");
        } else if (accounts.isEmpty()) {
            return ServiceResponse.success("No account found for user id: " + userId, false);
        }

        boolean isOwn = accounts.stream().anyMatch(account -> account.getAccountUuid().equals(uuid));
        if (!isOwn) {
            return ServiceResponse.success("User doesn't own account: " + uuid, false);
        }
        return ServiceResponse.success("User owns account: " + uuid, true);
    }
}

package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;

import java.util.List;

public interface AccountService extends IService<Account> {
    ServiceResponse<Account> getAccountByUuid(String uuid);

    ServiceResponse<List<Account>> getAllAccountsByUserId(Long userId);

    ServiceResponse<?> createAccount(Account account, Long userId, String clientId);

    ServiceResponse<?> updateAccountName(String uuid, String newName, Long userId, String clientId);

    ServiceResponse<?> updateAccountInGameUid(String uuid, String newInGameUid, Long userId, String clientId);

    ServiceResponse<?> deleteAccount(String uuid, Long userId, String clientId);

    ServiceResponse<Boolean> isUserOwnAccount(Long userId, String uuid);
}

package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;

import java.util.List;

public interface AccountService extends IService<Account> {
    ServiceResponse<Account> getAccountByUuid(String uuid);

    ServiceResponse<List<Account>> getAllAccountsByUserId(Long userId);

    ServiceResponse<?> createAccount(Account account);

    ServiceResponse<?> updateAccountName(String uuid, String newName);

    ServiceResponse<?> updateAccountInGameUid(String uuid, String newInGameUid);

    ServiceResponse<?> deleteAccount(String uuid);
}

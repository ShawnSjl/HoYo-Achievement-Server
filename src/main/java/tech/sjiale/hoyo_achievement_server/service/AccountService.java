package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameType;

import java.util.List;

public interface AccountService extends IService<Account> {
    ServiceResponse<Account> getAccountByUuid(String uuid);

    ServiceResponse<List<Account>> getAllAccountsByUserId(Long userId);

    void createAccount(Account account);

    void updateAccountName(String uuid, String newName);

    void updateAccountInGameUid(String uuid, String newInGameUid);

    void deleteAccount(String uuid);
}

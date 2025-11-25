package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.mapper.AccountMapper;

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
}

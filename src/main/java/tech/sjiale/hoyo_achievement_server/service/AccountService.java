package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.Account;

public interface AccountService extends IService<Account> {
    ServiceResponse<Account> getAccountByUuid(String uuid);
}

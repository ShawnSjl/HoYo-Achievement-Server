package tech.sjiale.hoyo_achievement_server.dto.account_request;

import lombok.Data;

@Data
public class AccountUpdateUidRequest {
    private String accountUuid;
    private String accountInGameUid;
}

package tech.sjiale.hoyo_achievement_server.dto.account_request;

import lombok.Data;

@Data
public class AccountUpdateNameRequest {
    private String accountUuid;
    private String accountName;
}

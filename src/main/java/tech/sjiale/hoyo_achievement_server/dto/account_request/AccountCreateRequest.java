package tech.sjiale.hoyo_achievement_server.dto.account_request;

import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameType;

@Data
public class AccountCreateRequest {
    private String accountUuid;
    private GameType gameType;
    private String accountName;
    private String accountInGameUid;
}

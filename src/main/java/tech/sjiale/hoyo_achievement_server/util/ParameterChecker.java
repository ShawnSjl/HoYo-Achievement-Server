package tech.sjiale.hoyo_achievement_server.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParameterChecker {

    /**
     * Checking user id; user id should be a positive integer
     *
     * @param id user id
     * @return true if invalid, false otherwise
     */
    public static boolean isUserIdInvalid(String id) {
        if (id == null) return true;
        return !id.matches("^\\d+$");
    }

    /**
     * Checking username; username should between 3-20 length, it could include English, Chinese, digits and underscore
     *
     * @param username username
     * @return true if invalid, false otherwise
     */
    public static boolean isUsernameInvalid(String username) {
        if (username == null) return true;
        return !username.matches("^[\\u4e00-\u9fa5_a-zA-Z0-9]{3,20}$");
    }

    /**
     * Checking password; password should between 8-50 length, it could include at least one English letter and one
     * digit, and accepted characters include English letters, digits, special characters
     *
     * @param password password
     * @return true if invalid, false otherwise
     */
    public static boolean isPasswordInvalid(String password) {
        if (password == null) return true;
        return !password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{}]{8,50}$");
    }

    /**
     * Helper method to check if the account uuid is invalid
     *
     * @param accountUuid account uuid
     * @return true if invalid, false otherwise
     */
    public static boolean isAccountUuidInvalid(String accountUuid) {
        boolean invalid = accountUuid == null
                || accountUuid.isBlank()
                || accountUuid.length() > 512;
        if (invalid) {
            log.error("Account uuid is invalid: {}.", accountUuid);
        }
        return invalid;
    }

    /**
     * Helper method to check if the account name is invalid
     *
     * @param accountName account name
     * @return true if invalid, false otherwise
     */
    public static boolean isAccountNameInvalid(String accountName) {
        boolean invalid = accountName == null
                || accountName.isBlank()
                || accountName.length() > 24;
        if (invalid) {
            log.error("Account name is invalid: {}.", accountName);
        }
        return invalid;
    }

    /**
     * Helper method to check if the account in game uid is invalid
     *
     * @param accountInGameUid account in game uid
     * @return true if invalid, false otherwise
     */
    public static boolean isAccountInGameUidInvalid(String accountInGameUid) {
        if (accountInGameUid == null) return true;

        boolean invalid = accountInGameUid.length() > 128;
        if (invalid) {
            log.error("Account in game uid is too long.");
        }
        return invalid;
    }
}

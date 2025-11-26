package tech.sjiale.hoyo_achievement_server.util;

public class ParameterChecker {

    /**
     * Checking user id; user id should be a positive integer
     *
     * @param id user id
     * @return true if valid, false otherwise
     */
    public static boolean isValidUserId(String id) {
        if (id == null) return false;
        return id.matches("^\\d+$");
    }

    /**
     * Checking username; username should between 3-20 length, it could include English, Chinese, digits and underscore
     *
     * @param username username
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.matches("^[\\u4e00-\u9fa5_a-zA-Z0-9]{3,20}$");
    }

    /**
     * Checking password; password should between 8-50 length, it could include at least one English letter and one
     * digit, and accepted characters include English letters, digits, special characters
     *
     * @param password password
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{}]{8,50}$");
    }
}

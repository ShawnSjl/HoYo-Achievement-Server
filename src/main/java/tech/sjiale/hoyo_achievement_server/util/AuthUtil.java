package tech.sjiale.hoyo_achievement_server.util;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthUtil {

    /**
     * Helper method to check if the user is login
     *
     * @return true if not login, false otherwise
     */
    public static boolean isNotLogin() {
        boolean login = StpUtil.isLogin();
        if (!login) {
            log.error("User is not login.");
        }
        return !login;
    }
    
}

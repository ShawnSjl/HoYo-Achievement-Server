package tech.sjiale.hoyo_achievement_server;

import cn.dev33.satoken.SaManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class HoYoAchievementServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoYoAchievementServerApplication.class, args);
        log.info("HoYo Achievement Server start success!");
        log.info("Sa-Token config: {}", SaManager.getConfig());
    }

}

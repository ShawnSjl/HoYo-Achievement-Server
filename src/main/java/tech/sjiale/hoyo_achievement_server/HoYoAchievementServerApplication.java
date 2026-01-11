package tech.sjiale.hoyo_achievement_server;

import cn.dev33.satoken.SaManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@MapperScan("tech.sjiale.hoyo_achievement_server.mapper")
public class HoYoAchievementServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoYoAchievementServerApplication.class, args);
        log.info("Sa-Token config: {}", SaManager.getConfig());
        log.info("HoYo Achievement Server start success!");
        printAsciiArt();
    }

    private static void printAsciiArt() {
        String art = """
                
                  _    _   __     __                  _     _                                     _  \s
                 | |  | |  \\ \\   / /        /\\       | |   (_)                                   | | \s
                 | |__| | __\\ \\_/ /__      /  \\   ___| |__  _  _____   _____ _ __ ___   ___ _ __ | |_\s
                 |  __  |/ _ \\   / _ \\    / /\\ \\ / __| '_ \\| |/ _ \\ \\ / / _ \\ '_ ` _ \\ / _ \\ '_ \\| __|
                 | |  | | (_) | | (_) |  / ____ \\ (__| | | | |  __/\\ V /  __/ | | | | |  __/ | | | |_\s
                 |_|  |_|\\___/|_|\\___/  /_/    \\_\\___|_| |_|_|\\___| \\_/ \\___|_| |_| |_|\\___|_| |_|\\__|
                
                """;
        log.info(art);
    }
}

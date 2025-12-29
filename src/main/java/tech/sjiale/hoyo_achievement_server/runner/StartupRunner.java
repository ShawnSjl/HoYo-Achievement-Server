package tech.sjiale.hoyo_achievement_server.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tech.sjiale.hoyo_achievement_server.dto.MigrationResult;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.service.MigrationService;
import tech.sjiale.hoyo_achievement_server.service.UserService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final MigrationService migrationService;
    private final UserService userService;

    @Value("${app.data-folder}")
    private String dataFolder;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Start to import new data from local data folder");
        ServiceResponse<List<MigrationResult>> response = migrationService.importNewData(dataFolder);
        if (!response.success()) {
            log.error("Import new data failed. {}", response.message());
        }

        log.info("Check root user status");
        try {
            // TODO 尝试使用log作为显示密码的地方
            ServiceResponse<?> rootStatus = userService.createRootUser("root", "Root@123");
            log.info("Root user status: {}", rootStatus.message());
        } catch (Exception e) {
            log.error("Create root user failed.", e);
        }
    }
}

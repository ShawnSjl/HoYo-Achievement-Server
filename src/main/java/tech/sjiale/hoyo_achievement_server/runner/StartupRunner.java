package tech.sjiale.hoyo_achievement_server.runner;

import cn.hutool.core.util.StrUtil;
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
import tech.sjiale.hoyo_achievement_server.util.PasswordGenerator;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final MigrationService migrationService;
    private final UserService userService;

    // args or yaml: --app.admin.initial-password=xxxxx
    @Value("${app.admin.initial-password:#{null}}")
    private String configuredPassword;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Start to import new data from local data folder");
        ServiceResponse<List<MigrationResult>> response = migrationService.importNewData();
        if (!response.success()) {
            log.error("Import new data failed. {}", response.message());
        }

        log.info("Check root user status");
        try {
            // Check if the root user exists
            ServiceResponse<Boolean> existResponse = userService.hasRootUser();
            // If the root user exists, do nothing
            if (existResponse.success() && existResponse.data()) {
                log.info(existResponse.message());
            }
            // If the root user doesn't exist, create a new one
            else {
                // Get the password from config or generate a random one
                String finalPassword;
                if (StrUtil.isNotBlank(configuredPassword)) {
                    finalPassword = configuredPassword;
                    log.info("Creating root user with configured password.");
                } else {
                    finalPassword = PasswordGenerator.generatePassword(12);
                    log.info("Creating root user with random password.");
                }

                // Create the root user
                ServiceResponse<?> createStatus = userService.createRootUser("root", finalPassword);
                if (createStatus.success()) {
                    log.info(createStatus.message());
                    // Print password to console
                    if (!StrUtil.isNotBlank(configuredPassword)) {
                        printBanner(finalPassword);
                    }
                } else {
                    log.error(createStatus.message());
                }
            }
        } catch (Exception e) {
            log.error("Create root user failed.", e);
        }
    }

    private void printBanner(String password) {
        String banner = """
                
                ##########################################################
                ##                                                      ##
                ##   [INITIALIZATION] Root User Created Successfully    ##
                ##                                                      ##
                ##   Username: root                                     ##
                ##   Password: %-33s##
                ##                                                      ##
                ##   PLEASE CHANGE THIS PASSWORD AFTER FIRST LOGIN!     ##
                ##                                                      ##
                ##########################################################
                """.formatted(password);
        log.info(banner);
    }
}

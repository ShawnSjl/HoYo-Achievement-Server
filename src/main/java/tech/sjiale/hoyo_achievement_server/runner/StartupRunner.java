package tech.sjiale.hoyo_achievement_server.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.service.MigrationService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final MigrationService migrationService;

    @Value("${app.data-folder}")
    private String data_folder;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Start to import new data from {}", data_folder);
        ServiceResponse<List<String>> response = migrationService.importNewData(data_folder);
        if (response.success()) {
            for (String file : response.data()) {
                log.info("Import new data from file: {}", file);
            }
        } else {
            log.info("Import new data failed. {}", response.message());
        }
    }
}

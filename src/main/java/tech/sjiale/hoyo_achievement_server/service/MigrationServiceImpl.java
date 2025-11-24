package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.sjiale.hoyo_achievement_server.dto.MigrationFile;
import tech.sjiale.hoyo_achievement_server.dto.MigrationOperation;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataMigration;
import tech.sjiale.hoyo_achievement_server.mapper.DataMigrationMapper;
import tech.sjiale.hoyo_achievement_server.mapper.ServerInfoMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service("migrationService")
@RequiredArgsConstructor
public class MigrationServiceImpl extends ServiceImpl<DataMigrationMapper, DataMigration> implements MigrationService{

    private final ServerInfoService serverInfoService;

    /** Migrate Data when the server starts. Should only be called once.
     * @param dirPath directory path; should be a directory */
    public void initialMigration(String dirPath) {
        // check path is a directory
        Path p = Paths.get(dirPath);
        if (!Files.isDirectory(p)) {
            log.error("Given path is not a directory: {}", dirPath);
        } else {
            log.debug("Start to get migration data in directory: {}", dirPath);
        }

        // get all JSON files in the directory
        List<String> jsonFiles = findJSONInDirectory(dirPath);

        // handle JSON files
        for (String jsonFile : jsonFiles) {
            handleJSONFile(jsonFile);
        }
    }

    /** Import new data from a directory.
     * Should only be called after initialMigration and server started.
     * @param path path; could be a directory or a file */
    public ServiceResponse<?> importNewData(String path) {
        Path p = Paths.get(path);
        if (Files.isDirectory(p)) {
            log.debug("Start to get new data in directory: {}", path);
            List<String> jsonFiles = findJSONInDirectory(path);

            // Return error if no JSON file found in the directory
            if (jsonFiles.isEmpty()) {
                log.warn("No JSON file found in directory: {}", path);
                return ServiceResponse.error("No JSON file found in directory: " + path);
            }

            // handle JSON files
            List<String> successFiles = new ArrayList<>();
            for (String jsonFile : jsonFiles) {
                if (handleJSONFile(jsonFile)) {
                    successFiles.add(jsonFile);
                }
            }

            // Return error if no JSON file imported successfully
            if (successFiles.isEmpty()) {
                log.warn("No JSON file imported successfully.");
                return ServiceResponse.error("No JSON file imported successfully.");
            }

            log.debug("Import new data from directory successfully.");
            return ServiceResponse.success("Import new data successfully.", successFiles);
        } else if (Files.isRegularFile(p) && path.endsWith(".json")) {
            log.debug("Start to get new data in file: {}", path);

            if (handleJSONFile(path)) {
                log.debug("Import new data successfully.");
                return ServiceResponse.success("Import new data successfully.");
            } else {
                log.warn("Import new data failed.");
                return ServiceResponse.error("Import new data failed.");
            }
        } else {
            log.error("Given path is not a directory or a JSON file: {}", path);
            return ServiceResponse.error("Given path is not a directory or a JSON file: " + path);
        }
    }

    private boolean handleJSONFile(String jsonFile) {
        log.debug("handle JSON file: {}", jsonFile);

        ObjectMapper mapper = new ObjectMapper();
        MigrationFile migrationFile;

        // Try to read and parse the JSON file
        try {
            byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonFile));
            migrationFile = mapper.readValue(jsonBytes, MigrationFile.class);
        } catch (IOException e) {
            log.error("Failed to read or parse JSON file: {} | error: {}", jsonFile, e.getMessage());
            return false;
        }

        // Check if the JSON file is in valid format
        if (migrationFile.name() == null || migrationFile.name().isBlank()) {
            log.error("Invalid JSON file: 'name' is missing. file={}", jsonFile);
            return false;
        }
        if (migrationFile.type() == null || migrationFile.type().isBlank()) {
            log.error("Invalid JSON file: 'type' is missing. file={}", jsonFile);
            return false;
        }
        if (migrationFile.payload() == null) {
            log.error("Invalid JSON file: 'payload' is missing. file={}", jsonFile);
            return false;
        }

        // Handle different types of JSON file
        return switch (migrationFile.type()) {
            case "data", "patch" -> handleJSON(migrationFile, jsonFile);
            default -> {
                log.error("Unknown migration type '{}' in file {}", migrationFile.type(), jsonFile);
                yield false;
            }
        };
    }

    private boolean handleJSON(MigrationFile migrationFile, String filePath) {
        log.debug("Handle data file: {}", filePath);

        // Check dependencies
        for (String dependency : migrationFile.depends()) {
            List<DataMigration> migration = this.lambdaQuery().eq(DataMigration::getName, dependency).list();
            if (migration.isEmpty()) {
                log.debug("Dependency '{}' not satisfy for file {}", dependency, filePath);
                return false;
            } else if (migration.size() > 1) {
                log.error("Return multiple dependency '{}', dependency should unique in database", dependency);
                return false;
            }
        }

        // Apply operations in payload
        for (MigrationOperation operation : migrationFile.payload().operations()) {
            // Check if operation is valid
            if (operation.table() == null || operation.table().isBlank()) {
                log.error("Invalid operation: 'table' is missing. file={}", filePath);
                return false;
            }

            switch(operation.action()) {
                case "insert":
                    if(!handleInsert(operation.table(), operation.values())) {
                        return false;
                    }
                    break;

                case "update":
                    log.warn("Not implemented yet");
                    return false;

                default:
                    log.error("Unknown action '{}'", operation.action());
                    return false;
            }
        }
        return true;
    }

    private boolean handleInsert(String table, List<Map<String, Object>> data) {
        switch (table) {
            case "server_info":
                ServiceResponse<?> res = serverInfoService.insertServerInfoBatch(data);
                return res.success();
            default:
                log.error("Unknown table type '{}'", table);
                return false;
        }
    }

    /** Find all JSON files in a directory.
     * It will check all subdirectories as well.
     * The maximum depth is 5.
     * @param dirPath directory path; should be a directory
     * @return a list of JSON file paths */
    private static List<String> findJSONInDirectory(String dirPath) {
        // parse the directory path, assert it is a directory
        Path path = Paths.get(dirPath);
        assert(Files.isDirectory(path));

        // result list
        List<String> foundFiles = new ArrayList<>();

        // walk through the directory and subdirectories and find all JSON files
        try (Stream<Path> stream = Files.walk(path, 5)) {
            stream.forEach(filePath -> {
                if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".json")) {
                    foundFiles.add(filePath.toString());
                    log.debug("found JSON file: {}", filePath);
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return foundFiles;
    }

}

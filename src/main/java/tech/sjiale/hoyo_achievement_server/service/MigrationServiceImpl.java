package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.MigrationFile;
import tech.sjiale.hoyo_achievement_server.dto.MigrationOperation;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.DataMigration;
import tech.sjiale.hoyo_achievement_server.mapper.DataMigrationMapper;

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
public class MigrationServiceImpl extends ServiceImpl<DataMigrationMapper, DataMigration> implements MigrationService {

    @Autowired
    @Lazy
    private MigrationService self;

    private final ServerInfoService serverInfoService;
    private final SrAchievementService srAchievementService;
    private final SrBranchService srBranchService;
    private final ZzzAchievementService zzzAchievementService;
    private final ZzzBranchService zzzBranchService;

    /**
     * Import new data from a directory or a JSON file.
     *
     * @param path path; could be a directory or a file
     * @return ServiceResponse
     */
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
                if (self.handleJSONFile(jsonFile)) {
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

            if (self.handleJSONFile(path)) {
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

    /**
     * Find all JSON files in a directory.
     * It will check all subdirectories as well.
     * The maximum depth is 5.
     *
     * @param dirPath directory path; should be a directory
     * @return a list of JSON file paths
     */
    private static List<String> findJSONInDirectory(String dirPath) {
        // parse the directory path, assert it is a directory
        Path path = Paths.get(dirPath);
        assert (Files.isDirectory(path));

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

    /**
     * Handle a JSON file; This is a transaction, It will roll back and throw an exception if failed
     *
     * @param jsonFile JSON file path
     * @return true if successfully handle the JSON file, false if the format invalid
     */
    @Transactional
    public boolean handleJSONFile(String jsonFile) {
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

        // Check if the migration file has been imported before
        {
            DataMigration migration = this.lambdaQuery()
                    .eq(DataMigration::getName, migrationFile.name())
                    .one();
            if (migration != null) {
                log.debug("Migration file '{}' has been imported before.", migrationFile.name());
                return true;
            }
        }

        // Handle different types of JSON file
        return switch (migrationFile.type()) {
            case "data", "patch" -> {
                // If handle successfully, insert into the database
                if (handleJSON(migrationFile, jsonFile)) {
                    DataMigration migration = new DataMigration();
                    migration.setName(migrationFile.name());
                    migration.setPath(jsonFile);
                    migration.setType(migrationFile.type());
                    migration.setDepends(migrationFile.depends());

                    // Save the migration record to the database
                    boolean saved = this.save(migration);
                    if (saved) yield true;
                    else {
                        throw new RuntimeException("Failed to save migration record to database.");
                    }
                } else {
                    yield false;
                }
            }
            default -> {
                log.error("Unknown migration type '{}' in file {}", migrationFile.type(), jsonFile);
                yield false;
            }
        };
    }

    /**
     * Handle JSON with type 'data' or 'patch'; It will throw an exception if failed
     *
     * @param migrationFile migration file
     * @param filePath      JSON file path
     * @return true if successfully handle all operations, false if the format invalid
     */
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
            // Check if the operation is valid
            if (operation.table() == null || operation.table().isBlank()) {
                log.error("Invalid operation: 'table' is missing. file={}", filePath);
                return false;
            }

            switch (operation.action()) {
                case "insert":
                    handleInsert(operation.table(), operation.values());
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

    /**
     * Handle insert operation; It will throw an exception if failed
     *
     * @param table table name
     * @param data  insert data
     */
    private void handleInsert(String table, List<Map<String, Object>> data) {
        ServiceResponse<?> res = switch (table) {
            case "server_info" -> serverInfoService.insertServerInfoBatch(data);
            case "sr_achievement" -> srAchievementService.insertAchievements(data);
            case "sr_branch" -> srBranchService.insertBranches(data);
            case "zzz_achievement" -> zzzAchievementService.insertAchievements(data);
            case "zzz_branch" -> zzzBranchService.insertBranches(data);
            default -> {
                log.error("Unknown table type '{}'", table);
                throw new IllegalArgumentException("Unknown table type '" + table + "'");
            }
        };

        // If not success, throw an exception
        if (!res.success()) {
            throw new RuntimeException("Insert failed for table: " + table);
        }
    }

}

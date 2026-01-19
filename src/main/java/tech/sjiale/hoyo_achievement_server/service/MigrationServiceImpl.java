package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.*;
import tech.sjiale.hoyo_achievement_server.entity.DataMigration;
import tech.sjiale.hoyo_achievement_server.mapper.DataMigrationMapper;
import tech.sjiale.hoyo_achievement_server.util.GitUtils;

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

    @Value("${app.data-folder}")
    private String dataFolder;
    @Value("${app.enable-jgit}")
    private boolean enableJGit;
    @Value("${app.data-repo-url}")
    private String dataUrl;

    private final ServerInfoService serverInfoService;
    private final SrAchievementService srAchievementService;
    private final SrBranchService srBranchService;
    private final ZzzAchievementService zzzAchievementService;
    private final ZzzBranchService zzzBranchService;

    /**
     * Get all data migration record; file's path are hidden
     *
     * @return all DataMigration
     */
    public ServiceResponse<List<DataMigration>> getAllMigrationRecord() {
        List<DataMigration> rowList = this.list();
        if (rowList == null || rowList.isEmpty()) {
            return ServiceResponse.error("No data migration record found.");
        }

        // Hidden the real path
        for (DataMigration record : rowList) {
            if (record.getPath().startsWith(dataFolder)) {
                record.setPath("LOCAL");
            } else {
                record.setPath("UPLOAD");
            }
        }

        return ServiceResponse.success("Get all data migration record success.", rowList);
    }

    /**
     * Import new data from a directory.
     *
     * @return ServiceResponse
     */
    public ServiceResponse<List<MigrationResult>> importNewData() {
        Path p = Paths.get(dataFolder);
        if (Files.isDirectory(p)) {

            // Pull data from the remote repository if enabled
            if (enableJGit) {
                log.info("JGit is enabled, will pull data from remote repository: {}", dataUrl);
                GitUtils.cloneOrPull(dataUrl, dataFolder);
            }

            // Find all JSON files in the directory
            log.info("Start to get new data in directory: {}", dataFolder);
            List<String> jsonFiles = findJSONInDirectory(dataFolder);

            // Return error if no JSON file found in the directory
            if (jsonFiles.isEmpty()) {
                log.warn("No JSON file found in directory: {}", dataFolder);
                return ServiceResponse.error("No JSON file found in directory: " + dataFolder);
            }

            // Return list
            List<MigrationResult> results = new ArrayList<>();

            // handle JSON files
            List<String> failedFiles = new ArrayList<>();
            for (String jsonFile : jsonFiles) {
                MigrationResult result = self.handleJSONFile(jsonFile);
                if (result.status() == ImportStatus.FAIL) {
                    failedFiles.add(jsonFile);
                } else {
                    results.add(result);
                }
            }
            // Second chance for failed files
            for (String failedFile : failedFiles) {
                MigrationResult result = self.handleJSONFile(failedFile);
                if (result.status() == ImportStatus.FAIL) {
                    log.warn("Failed to import file twice: {}", failedFile);
                }
                results.add(result);
            }

            log.debug("Import new data from directory successfully.");
            return ServiceResponse.success("Import new data successfully.", results);
        } else {
            log.error("Given path is not a directory: {}", dataFolder);
            return ServiceResponse.error("Given path is not a directory: " + dataFolder);
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

        // sort the list
        foundFiles.sort(String::compareTo);
        return foundFiles;
    }

    /**
     * Handle a JSON file; This is a transaction, It will roll back and throw an exception if failed
     *
     * @param jsonFile JSON file path
     * @return MigrationResult
     */
    @Transactional
    public MigrationResult handleJSONFile(String jsonFile) {
        log.debug("handle JSON file: {}", jsonFile);

        ObjectMapper mapper = new ObjectMapper();
        MigrationFile migrationFile;

        // Try to read and parse the JSON file
        try {
            byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonFile));
            migrationFile = mapper.readValue(jsonBytes, MigrationFile.class);
        } catch (IOException e) {
            log.warn("Failed to read or parse JSON file: {} | error: {}", jsonFile, e.getMessage());
            return MigrationResult.failed(jsonFile, "Failed to read or parse JSON file");
        }

        // Check if the JSON file is in valid format
        if (migrationFile.name() == null || migrationFile.name().isBlank()) {
            log.warn("Invalid JSON file: 'name' is missing. file={}", jsonFile);
            return MigrationResult.failed(jsonFile, "Invalid JSON file: 'name' is missing");
        }
        if (migrationFile.type() == null || migrationFile.type().isBlank()) {
            log.warn("Invalid JSON file: 'type' is missing. file={}", jsonFile);
            return MigrationResult.failed(jsonFile, "Invalid JSON file: 'type' is missing");
        }
        if (migrationFile.payload() == null) {
            log.warn("Invalid JSON file: 'payload' is missing. file={}", jsonFile);
            return MigrationResult.failed(jsonFile, "Invalid JSON file: 'payload' is missing");
        }

        // Check if the migration file has been imported before
        {
            DataMigration migration = this.lambdaQuery()
                    .eq(DataMigration::getName, migrationFile.name())
                    .one();
            if (migration != null) {
                log.info("Migration file '{}' has been imported before.", migrationFile.name());
                return MigrationResult.imported(migrationFile.name());
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
                    if (saved) {
                        log.info("Migration file '{}' imported successfully.", migrationFile.name());
                        yield MigrationResult.success(migrationFile.name());
                    } else {
                        throw new RuntimeException("Failed to save migration record to database.");
                    }
                } else {
                    yield MigrationResult.failed(migrationFile.name(), "Failed to handle JSON file");
                }
            }
            default -> {
                log.warn("Unknown migration type '{}' in file {}", migrationFile.type(), jsonFile);
                yield MigrationResult.failed(migrationFile.name(), "Unknown migration type '" + migrationFile.type() + "'");
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
                log.warn("Invalid operation: 'table' is missing. file={}", filePath);
                return false;
            }

            switch (operation.action()) {
                case "insert":
                    handleInsert(operation.table(), operation.values());
                    break;

                case "update":
                    handleUpdate(operation.table(), operation.values());
                    break;

                case "delete":
                    handleDelete(operation.table(), operation.values());
                    break;

                default:
                    log.warn("Unknown action '{}'", operation.action());
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
            case "sr_achievement" -> srAchievementService.insertAchievementBatch(data);
            case "sr_branch" -> srBranchService.insertBranchBatch(data);
            case "zzz_achievement" -> zzzAchievementService.insertAchievementBatch(data);
            case "zzz_branch" -> zzzBranchService.insertBranchBatch(data);
            default -> {
                log.warn("Unknown table type '{}' for insert", table);
                throw new IllegalArgumentException("Unknown table type '" + table + "' for insert");
            }
        };

        // If not success, throw an exception
        if (!res.success()) {
            throw new RuntimeException("Insert failed for table: " + table);
        }
    }

    /**
     * Handle update operation; It will throw an exception if failed
     *
     * @param table table name
     * @param data  update data
     */
    private void handleUpdate(String table, List<Map<String, Object>> data) {
        ServiceResponse<?> res = switch (table) {
            case "server_info" -> serverInfoService.updateServerInfoBatch(data);
            case "sr_achievement" -> srAchievementService.updateAchievementBatch(data);
            case "sr_branch" -> srBranchService.updateBranchBatch(data);
            case "zzz_achievement" -> zzzAchievementService.updateAchievementBatch(data);
            case "zzz_branch" -> zzzBranchService.updateBranchBatch(data);
            default -> {
                log.warn("Unknown table type '{}' for update", table);
                throw new IllegalArgumentException("Unknown table type '" + table + "' for update");
            }
        };

        // If not success, throw an exception
        if (!res.success()) {
            throw new RuntimeException("Update failed for table: " + table);
        }
    }

    /**
     * Handle delete operation; It will throw an exception if failed
     *
     * @param table table name
     * @param data  delete data
     */
    private void handleDelete(String table, List<Map<String, Object>> data) {
        ServiceResponse<?> res = switch (table) {
            case "server_info" -> serverInfoService.deleteServerInfoBatch(data);
            case "sr_achievement" -> srAchievementService.deleteAchievementBatch(data);
            case "sr_branch" -> srBranchService.deleteBranchBatch(data);
            case "zzz_achievement" -> zzzAchievementService.deleteAchievementBatch(data);
            case "zzz_branch" -> zzzBranchService.deleteBranchBatch(data);
            default -> {
                log.warn("Unknown table type '{}' for delete", table);
                throw new IllegalArgumentException("Unknown table type '" + table + "' for delete");
            }
        };

        // If not success, throw an exception
        if (!res.success()) {
            throw new RuntimeException("Delete failed for table: " + table);
        }
    }
}

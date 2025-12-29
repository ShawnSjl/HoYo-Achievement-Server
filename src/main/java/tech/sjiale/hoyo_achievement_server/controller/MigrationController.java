package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.util.SaResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.sjiale.hoyo_achievement_server.dto.ImportStatus;
import tech.sjiale.hoyo_achievement_server.dto.MigrationResult;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.service.MigrationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final MigrationService migrationService;

    @Value("${app.data-folder}")
    private String dataFolder;
    @Value("${app.upload-folder}")
    private String uploadFolder;

    @PutMapping("/local-data")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    @SaCheckSafe
    public SaResult migrateDataFolder() {
        // Return list, only includes the success and failed result
        List<MigrationResult> resultList = new ArrayList<>();

        // Import data from local data folder
        ServiceResponse<List<MigrationResult>> response = migrationService.importNewData(dataFolder);
        if (response.success()) {
            for (MigrationResult result : response.data()) {
                if (result.status() != ImportStatus.IMPORTED) {
                    resultList.add(result);
                }
            }
        } else {
            log.error("Import new data from data folder failed. {}", response.message());
        }

        return SaResult.ok("导入本地数据成功").setData(resultList);
    }

    @PostMapping("/upload")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    @SaCheckSafe
    public SaResult uploadFiles(@RequestParam("file") MultipartFile[] files) {
        // Check if files are uploaded
        if (files == null || files.length == 0) {
            log.warn("No file uploaded.");
            return SaResult.error("请选择上传的文件").setCode(HttpStatus.BAD_REQUEST.value());
        }

        // Return list, only includes the success and failed result
        List<MigrationResult> resultList = new ArrayList<>();
        Map<String, String> fileNameMap = new HashMap<>();

        // Try to save files to upload folder
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // Try to save file; if false, store result to list; if true, store original file name to map
            try {
                String newFileName = saveFile(file);
                if (newFileName == null) {
                    resultList.add(MigrationResult.failed(file.getOriginalFilename(), "非JSON文件"));
                } else {
                    fileNameMap.put(newFileName, file.getOriginalFilename());
                }
            } catch (Exception e) {
                log.error("Save file failed. {}", e.getMessage());
                resultList.add(MigrationResult.failed(file.getOriginalFilename(), e.getMessage()));
            }
        }

        // Import data from local data folder
        ServiceResponse<List<MigrationResult>> response = migrationService.importNewData(uploadFolder);
        if (response.success()) {
            for (MigrationResult result : response.data()) {
                String originalName = fileNameMap.get(result.fileName());
                switch (result.status()) {
                    case SUCCESS: {
                        if (originalName != null) {
                            resultList.add(MigrationResult.success(originalName));
                        } else {
                            resultList.add(result);
                        }
                        break;
                    }
                    case IMPORTED: {
                        if (originalName != null) {
                            resultList.add(MigrationResult.imported(originalName));
                        }
                        deleteFile(result.fileName());
                        break;
                    }
                    case FAIL: {
                        if (originalName != null) {
                            resultList.add(MigrationResult.failed(originalName, result.message()));
                        } else {
                            resultList.add(result);
                        }
                        deleteFile(result.fileName());
                        break;
                    }
                    default:
                        throw new RuntimeException("Unknow result type");
                }
            }
        } else {
            log.error("Import new data from upload folder failed. {}", response.message());
        }

        return SaResult.ok("导入上传数据成功").setData(resultList);
    }


    /**
     * Save uploaded JSON file; throw error if there are IO errors
     *
     * @param file uploaded JSON file
     * @return new file name
     */
    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty.");
        }

        // Get the complete directory path
        File directory = new File(uploadFolder);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }

        // Create the directory if not exists
        String extension = getExtension(file);
        if (!extension.equals("json") && !extension.equals("JSON")) {
            return null;
        }

        // Generate new file name and save it
        String newFileName = UUID.randomUUID() + "." + extension;
        File destFile = new File(directory, newFileName);
        try {
            file.transferTo(destFile);
            return newFileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file: " + e);
        }
    }

    /**
     * Get extension of file; throw error if the file extension name is illegal
     *
     * @param file uploaded file
     * @return file extension
     */
    private static @NonNull String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("File name not exist.");
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null ||
                extension.contains("/") ||
                extension.contains("\\") ||
                extension.contains("..")) {
            throw new RuntimeException("Invalid file extension");
        }
        return extension;
    }

    private boolean deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        try {
            // 1. 获取基础目录的 Path 对象
            Path rootLocation = Paths.get(uploadFolder).toAbsolutePath().normalize();

            // 2. 解析目标文件的路径
            Path destinationFile = rootLocation.resolve(fileName).normalize();

            // 3. 【关键安全检查】防止路径遍历攻击
            // 确保解析出来的路径，依然是以 rootLocation 开头的
            // 如果文件名是 "../../etc/passwd"，这里会检测出它不在 uploadFolder 下
            if (!destinationFile.startsWith(rootLocation)) {
                log.warn("检测到非法文件删除尝试: {}", fileName);
                return false;
            }

            // 4. 执行删除 (如果文件存在则删除，不存在返回 false，不会抛异常)
            boolean deleted = Files.deleteIfExists(destinationFile);

            if (deleted) {
                log.info("文件已删除: {}", fileName);
            } else {
                log.warn("文件删除失败（文件不存在）: {}", fileName);
            }
            return deleted;

        } catch (IOException e) {
            // 5. 捕获 IO 异常，避免因为删除失败导致整个接口报错
            // 通常清理工作失败记录日志即可，不应影响主业务流程
            log.error("文件删除出错: {}", fileName, e);
            return false;
        }
    }
}

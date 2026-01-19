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
import tech.sjiale.hoyo_achievement_server.entity.DataMigration;
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

    @GetMapping("/all")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    public SaResult getAllMigrationRecord() {
        ServiceResponse<List<DataMigration>> response = migrationService.getAllMigrationRecord();
        if (response.success()) {
            log.info(response.message());
            return SaResult.ok("获取全部数据导入记录成功").setData(response.data());
        } else {
            log.error(response.message());
            return SaResult.error("获取全部数据导入记录失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PostMapping("/load-local")
    @SaCheckLogin
    @SaCheckRole(value = {"ADMIN", "ROOT"}, mode = SaMode.OR)
    @SaCheckSafe
    public SaResult migrateDataFolder() {
        // Return list, only includes the success and failed result
        List<MigrationResult> resultList = new ArrayList<>();

        // Import data from local data folder
        ServiceResponse<List<MigrationResult>> response = migrationService.importNewData();
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
}

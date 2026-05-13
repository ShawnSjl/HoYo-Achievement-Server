package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerUpdateLog;
import tech.sjiale.hoyo_achievement_server.service.ServerUpdateLogService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/server")
@RequiredArgsConstructor
public class ServerUpdateLogController {

    private final ServerUpdateLogService serverUpdateLogService;

    /**
     * Get all server update log
     *
     * @return SaResult
     */
    @GetMapping("/all")
    public SaResult allServerInfo() {
        ServiceResponse<List<ServerUpdateLog>> response = serverUpdateLogService.getAllServerUpdateLog();
        if (response.success()) {
            log.info(response.message());
            return SaResult.ok("获取全部服务器信息成功").setData(response.data());
        } else {
            log.error(response.message());
            return SaResult.error("获取全部服务器信息失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Get the latest server update log
     *
     * @return SaResult
     */
    @GetMapping("/latest")
    public SaResult latestServerInfo(@RequestParam Long logID) {
        if (logID < 0) {
            return SaResult.error("非法Log ID").setCode(HttpStatus.BAD_REQUEST.value());
        }

        ServiceResponse<List<ServerUpdateLog>> response = serverUpdateLogService.getLatestServerUpdateLog(logID);
        if (response.success()) {
            log.info(response.message());
            return SaResult.ok("获取最新服务器信息成功").setData(response.data());
        } else {
            log.error(response.message());
            return SaResult.error("获取最新服务器信息失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}

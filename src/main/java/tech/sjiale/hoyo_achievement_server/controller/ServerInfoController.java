package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.ServerInfo;
import tech.sjiale.hoyo_achievement_server.service.ServerInfoService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/info")
@RequiredArgsConstructor
public class ServerInfoController {

    private final ServerInfoService serverInfoService;

    /**
     * Get all server info
     *
     * @return SaResult
     */
    @GetMapping("/all")
    public SaResult allServerInfo() {
        ServiceResponse<List<ServerInfo>> response = serverInfoService.getAllServerInfo();
        if (response.success()) {
            log.info(response.message());
            return SaResult.ok("获取全部服务器信息成功").setData(response.data());
        } else {
            log.error(response.message());
            return SaResult.error("获取全部服务器信息失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Get latest server info
     *
     * @return SaResult
     */
    @GetMapping("/latest")
    public SaResult latestServerInfo() {
        ServiceResponse<ServerInfo> response = serverInfoService.getLatestServerInfo();
        if (response.success()) {
            log.info(response.message());
            return SaResult.ok("获取最新服务器信息成功").setData(response.data());
        } else {
            log.error(response.message());
            return SaResult.error("获取最新服务器信息失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}

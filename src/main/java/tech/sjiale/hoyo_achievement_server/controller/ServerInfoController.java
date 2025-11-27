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
            log.info("Get all server info successfully.");
            return SaResult.ok().setData(response.data());
        } else {
            log.error("Get all server info failed: {}", response.message());
            return SaResult.error(response.message()).setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
            log.info("Get latest server info successfully.");
            return SaResult.ok().setData(response.data());
        } else {
            log.error("Get latest server info failed: {}", response.message());
            return SaResult.error(response.message()).setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}

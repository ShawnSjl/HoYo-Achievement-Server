package tech.sjiale.hoyo_achievement_server.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tech.sjiale.hoyo_achievement_server.service.SseServiceImpl;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseServiceImpl sseService;

    @GetMapping("/connect")
    public SseEmitter connect(@RequestParam String clientId) {
        // Get user id from token, 0 means all users
        long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : 0L;
        return sseService.connect(userId, clientId);
    }
}

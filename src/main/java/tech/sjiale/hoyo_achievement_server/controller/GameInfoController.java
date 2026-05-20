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
import tech.sjiale.hoyo_achievement_server.entity.GameInfo;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;
import tech.sjiale.hoyo_achievement_server.service.GameInfoService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameInfoController {

    private final GameInfoService gameInfoService;

//    /**
//     * Get all game info
//     *
//     * @return SaResult
//     */
//    @GetMapping("/all")
//    public SaResult getAllGameInfo() {
//        ServiceResponse<List<GameInfo>> response = gameInfoService.getAllGameInfo();
//        if (response.success()) {
//            log.info(response.message());
//            return SaResult.ok("获取全部游戏信息成功").setData(response.data());
//        } else {
//            log.error(response.message());
//            return SaResult.error("获取全部游戏信息失败").setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//    }

    /**
     * Get game info by game id
     *
     * @return SaResult
     */
    @GetMapping("/id")
    public SaResult getGameInfoByGameId(@RequestParam GameId gameId) {
        ServiceResponse<GameInfo> response = gameInfoService.getGameInfoByGameId(gameId);
        if (response.success()) {
            log.info(response.message());
            return SaResult.ok("获取游戏信息成功").setData(response.data());
        } else {
            log.error(response.message());
            return SaResult.error("获取游戏信息失败").setCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}

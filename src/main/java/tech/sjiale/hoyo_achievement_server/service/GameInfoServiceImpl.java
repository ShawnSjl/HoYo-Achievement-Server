package tech.sjiale.hoyo_achievement_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.GameInfo;
import tech.sjiale.hoyo_achievement_server.entity.nume.GameId;
import tech.sjiale.hoyo_achievement_server.mapper.GameInfoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("gameInfoService")
public class GameInfoServiceImpl extends ServiceImpl<GameInfoMapper, GameInfo> implements GameInfoService {

    /**
     * Get game info by game id
     *
     * @param gameId game id
     * @return ServiceResponse with GameInfo
     */
    public ServiceResponse<GameInfo> getGameInfoByGameId(GameId gameId) {
        GameInfo gameInfo = this.lambdaQuery()
                .eq(GameInfo::getGameId, gameId)
                .one();
        if (gameInfo == null) {
            return ServiceResponse.error("No game info found with game id: " + gameId);
        }
        return ServiceResponse.success("Get game info by game id successfully.", gameInfo);
    }

    /**
     * Insert game info batch; should only be called by migration service
     *
     * @param gameInfoMapList list of the game info map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> insertGameInfoBatch(List<Map<String, Object>> gameInfoMapList) {
        List<GameInfo> inserts = new ArrayList<>();

        for (Map<String, Object> gameInfoMap : gameInfoMapList) {
            GameInfo gameInfo = BeanUtil.toBean(gameInfoMap, GameInfo.class);

            // Check if all fields are filled
            if (BeanUtil.hasNullField(gameInfo)) {
                log.warn("Invalid game info for insert: {}, map: {}", gameInfo.toString(), gameInfoMap);
                return ServiceResponse.error("Invalid game info for insert.");
            }

            inserts.add(gameInfo);
        }

        // Save inserts results to the database
        if (!inserts.isEmpty()) {
            this.saveBatch(inserts);
        }

        log.debug("Insert game info batch successfully.");
        return ServiceResponse.success("Insert game info batch successfully.");
    }

    /**
     * Update game info batch; should only be called by migration service
     *
     * @param gameInfoMapList list of the game info map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateGameInfoBatch(List<Map<String, Object>> gameInfoMapList) {
        List<GameInfo> updates = new ArrayList<>();

        for (Map<String, Object> gameInfoMap : gameInfoMapList) {
            // Get target id from the map
            Object target = gameInfoMap.get("target");
            if (target == null) {
                log.warn("Invalid game info for update: missing 'target' for lookup.");
                throw new IllegalArgumentException("Invalid game info for update: missing 'target' for lookup.");
            }
            if (!(target instanceof Map)) {
                log.warn("Invalid server update log for update: 'target' is not a map.");
                throw new IllegalArgumentException("Invalid server update log for update: 'target' is not a map.");
            }
            Map<String, Object> targetMap = (Map<String, Object>) target;
            GameId gameId = GameId.valueOf(targetMap.get("game_id").toString());

            // Find target game info
            GameInfo targetGameInfo = this.getById(gameId);
            if (targetGameInfo == null) {
                log.warn("No game info found with id: {}", gameId);
                throw new IllegalArgumentException("No game info found with id: " + gameId);
            }

            // Update target game info
            BeanUtil.fillBeanWithMap(gameInfoMap, targetGameInfo,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setIgnoreCase(true)
                            .setIgnoreError(true)
            );

            // Handle the situation that id is changed
            GameId newID = targetGameInfo.getGameId();
            if (!gameId.equals(newID)) {
                UpdateWrapper<GameInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("game_id", gameId);
                updateWrapper.set("game_id", newID);
                boolean success = this.update(targetGameInfo, updateWrapper);
                if (!success) {
                    log.warn("Failed to update game info for id: {}", gameId);
                    throw new RuntimeException("Failed to update game info for id: " + gameId);
                }
            } else {
                // Save result to list
                updates.add(targetGameInfo);
            }
        }

        // Save updates results to the database
        if (!updates.isEmpty()) {
            this.updateBatchById(updates);
        }

        log.debug("Update game info batch successfully.");
        return ServiceResponse.success("Update game info batch successfully.");
    }

    /**
     * Delete game info batch; should only be called by migration service
     *
     * @param gameInfoMapList List of the game info map
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> deleteGameInfoBatch(List<Map<String, Object>> gameInfoMapList) {
        List<String> ids = new ArrayList<>();

        for (Map<String, Object> gameInfoMap : gameInfoMapList) {
            Object recordIdObj = gameInfoMap.get("record_id");
            if (recordIdObj == null) {
                log.warn("Invalid game info for delete: missing 'record_id' for lookup.");
                throw new IllegalArgumentException("Invalid game info for delete: missing 'record_id' for lookup.");
            }
            ids.add(recordIdObj.toString());
        }

        if (!ids.isEmpty()) {
            log.warn("No game info found for delete.");
            throw new IllegalArgumentException("No game info found for delete.");
        }

        // Delete records
        boolean success = this.removeByIds(ids);
        if (success) {
            log.debug("Delete game info batch successfully.");
            return ServiceResponse.success("Delete game info batch successfully.");
        } else {
            log.warn("Delete game info batch failed.");
            throw new RuntimeException("Delete game info batch failed.");
        }
    }
}

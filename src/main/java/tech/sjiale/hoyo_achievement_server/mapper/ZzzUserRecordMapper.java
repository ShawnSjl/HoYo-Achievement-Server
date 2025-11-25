package tech.sjiale.hoyo_achievement_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import tech.sjiale.hoyo_achievement_server.dto.ZzzAchievementRecordDto;
import tech.sjiale.hoyo_achievement_server.entity.ZzzUserRecord;

import java.util.List;

@Mapper
public interface ZzzUserRecordMapper extends BaseMapper<ZzzUserRecord> {
    @Select("""
                SELECT
                    za.achievement_id,
                    za.class_id,
                    za.name,
                    za.description,
                    za.reward_level,
                    za.hidden,
                    za.game_version,
                    0 AS complete
                FROM zzz_achievement za
                ORDER BY za.achievement_id
            """)
    List<ZzzAchievementRecordDto> selectAllAchievementsWithEmptyRecord();

    @Select("""
                SELECT
                    za.achievement_id,
                    za.class_id,
                    za.name,
                    za.description,
                    za.reward_level,
                    za.hidden,
                    za.game_version,
                    COALESCE(zur.complete, 0) AS complete
                FROM zzz_achievement za
                LEFT JOIN zzz_user_record zur
                    ON za.achievement_id = zur.achievement_id
                    AND zur.account_uuid = #{uuid}
                ORDER BY za.achievement_id
            """)
    List<ZzzAchievementRecordDto> selectAllAchievementsRecordByUUID(String uuid);
}

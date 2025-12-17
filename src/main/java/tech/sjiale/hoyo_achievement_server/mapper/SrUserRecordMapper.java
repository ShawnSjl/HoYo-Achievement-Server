package tech.sjiale.hoyo_achievement_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import tech.sjiale.hoyo_achievement_server.dto.SrAchievementRecordDto;
import tech.sjiale.hoyo_achievement_server.entity.SrUserRecord;

import java.util.List;

@Mapper
public interface SrUserRecordMapper extends BaseMapper<SrUserRecord> {
    @Select("""
                SELECT
                    sa.achievement_id,
                    sa.class_name,
                    sa.name,
                    sa.description,
                    sa.reward_level,
                    sa.game_version,
                    0 AS complete
                FROM sr_achievement sa
                ORDER BY sa.achievement_id
            """)
    List<SrAchievementRecordDto> selectAllAchievementsWithEmptyRecord();

    @Select("""
                SELECT
                    sa.achievement_id,
                    sa.class_name,
                    sa.name,
                    sa.description,
                    sa.reward_level,
                    sa.game_version,
                    COALESCE(sur.complete, 0) AS complete
                FROM sr_achievement sa
                LEFT JOIN sr_user_record sur
                    ON sa.achievement_id = sur.achievement_id
                    AND sur.account_uuid = #{uuid}
                ORDER BY sa.achievement_id
            """)
    List<SrAchievementRecordDto> selectAllAchievementsRecordByUUID(String uuid);
}

package tech.sjiale.hoyo_achievement_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import tech.sjiale.hoyo_achievement_server.dto.BasicAchievementDto;
import tech.sjiale.hoyo_achievement_server.entity.ZzzAchievement;

@Mapper
public interface ZzzAchievementMapper extends BaseMapper<ZzzAchievement> {
    BasicAchievementDto getBasicById(Integer achievementId);
}

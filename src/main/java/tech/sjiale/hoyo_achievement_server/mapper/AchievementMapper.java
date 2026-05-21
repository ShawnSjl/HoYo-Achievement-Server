package tech.sjiale.hoyo_achievement_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import tech.sjiale.hoyo_achievement_server.entity.Achievement;

@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
}

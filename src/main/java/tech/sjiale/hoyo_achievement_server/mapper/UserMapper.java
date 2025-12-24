package tech.sjiale.hoyo_achievement_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import tech.sjiale.hoyo_achievement_server.dto.user_request.UserExposeDto;
import tech.sjiale.hoyo_achievement_server.entity.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("""
                SELECT
                    id,
                    username,
                    role,
                    status,
                    created_at,
                    updated_at
                FROM users
                ORDER BY id;
            """)
    List<UserExposeDto> selectAll();
}

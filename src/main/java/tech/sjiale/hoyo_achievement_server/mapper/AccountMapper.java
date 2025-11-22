package tech.sjiale.hoyo_achievement_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import tech.sjiale.hoyo_achievement_server.entity.Account;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}

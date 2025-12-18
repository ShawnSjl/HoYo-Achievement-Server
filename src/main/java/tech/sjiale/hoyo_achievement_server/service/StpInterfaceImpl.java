package tech.sjiale.hoyo_achievement_server.service;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserMapper userMapper;


    @Override
    public List<String> getPermissionList(Object o, String s) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        // Get user ID
        Long userId = Long.valueOf(o.toString());

        User user = userMapper.selectById(userId);

        // Add the user's role to the list
        List<String> list = new ArrayList<>();
        if (user != null && user.getRole() != null) {
            list.add(user.getRole().name());
        }
        return list;
    }
}

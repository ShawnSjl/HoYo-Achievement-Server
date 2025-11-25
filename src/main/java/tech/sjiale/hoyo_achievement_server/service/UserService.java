package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;

import java.util.List;

public interface UserService extends IService<User> {
    ServiceResponse<User> getUserById(Integer id);

    ServiceResponse<User> getUserByName(String name);

    ServiceResponse<List<User>> getAllUsers();

    ServiceResponse<?> createUser(String username, String password);

    ServiceResponse<?> updateUsername(Integer id, String newUsername);

    ServiceResponse<?> updatePassword(Integer id, String newPassword);

    ServiceResponse<?> updateUserStatus(Integer id, UserStatus status);

    ServiceResponse<?> updateUserRole(Integer id, UserRole role);
}

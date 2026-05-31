package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.user_request.UserExposeDto;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;

import java.util.List;

public interface UserService extends IService<User> {
    ServiceResponse<User> getUserById(Long id);

    ServiceResponse<User> getUserByName(String name);

    ServiceResponse<List<UserExposeDto>> getAllUsers();

    ServiceResponse<?> createUser(String username, String password);

    ServiceResponse<?> updateUsername(Long userId, String clientId, String newUsername);

    ServiceResponse<?> updatePassword(Long userId, String newPassword);

    ServiceResponse<?> updateUserStatus(Long targetUserId, UserStatus status);

    ServiceResponse<?> updateUserRole(Long targetUserId, UserRole role);

    ServiceResponse<?> deleteUser(Long userId, String clientId);

    ServiceResponse<Boolean> isUserDisabled(Long id);

    // Startup Use
    ServiceResponse<Boolean> hasRootUser();

    ServiceResponse<?> createRootUser(String username, String password);
}

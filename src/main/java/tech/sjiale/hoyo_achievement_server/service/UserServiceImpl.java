package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;
import tech.sjiale.hoyo_achievement_server.mapper.UserMapper;

import java.util.List;

@Slf4j
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * Get user by id
     */
    public ServiceResponse<User> getUserById(Integer id) {
        User user = getById(id);
        if (user == null) {
            log.error("User id {} doesn't exist.", id);
            return ServiceResponse.error("User id doesn't exist.");
        }
        log.debug("Get user by id {} successfully.", id);
        return ServiceResponse.success("Get user by id successfully.", user);
    }

    /**
     * Get user by username
     */
    public ServiceResponse<User> getUserByName(String name) {
        User user = this.lambdaQuery().eq(User::getUsername, name).one();
        if (user == null) {
            log.error("User name {} doesn't exist.", name);
            return ServiceResponse.error("User name doesn't exist.");
        }
        log.debug("Get user by username {} successfully.", name);
        return ServiceResponse.success("Get user by username successfully.", user);
    }

    /**
     * Get all users
     */
    public ServiceResponse<List<User>> getAllUsers() {
        List<User> users = this.list();
        log.debug("Get all users successfully.");
        return ServiceResponse.success("Get all users successfully.", users);
    }

    /**
     * Create new user
     */
    @Transactional
    public ServiceResponse<?> createUser(String username, String password) {
        // Check if username already exists
        ServiceResponse<User> response = getUserByName(username);
        if (response.success()) {
            log.error("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists.");
        }

        User user = new User();
        user.setUsername(username);

        // TODO: check password format
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        this.save(user);
        log.debug("Create user {} successfully.", username);
        return ServiceResponse.success("Create user successfully.", user);
    }

    /**
     * Update username
     */
    @Transactional
    public ServiceResponse<?> updateUsername(Integer id, String newUsername) {
        // Check if username already exists
        ServiceResponse<User> response = getUserByName(newUsername);
        if (response.success()) {
            log.error("New username already exists: {}", newUsername);
            throw new IllegalArgumentException("New username already exists.");
        }

        this.lambdaUpdate().eq(User::getId, id).set(User::getUsername, newUsername).update();
        log.debug("Update username {} successfully.", newUsername);
        return ServiceResponse.success("Update username successfully.");
    }

    /**
     * Update user password
     */
    @Transactional
    public ServiceResponse<?> updatePassword(Integer id, String newPassword) {
        // TODO: check password format
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(newPassword);
        this.lambdaUpdate().eq(User::getId, id).set(User::getPassword, hashedPassword).update();
        log.debug("Update password successfully.");
        return ServiceResponse.success("Update password successfully.");
    }

    /**
     * Update user status
     */
    @Transactional
    public ServiceResponse<?> updateUserStatus(Integer id, UserStatus status) {
        // Check if user exists
        ServiceResponse<User> response = getUserById(id);
        if (!response.success()) {
            log.error("Target user {} doesn't exist.", id);
            throw new IllegalArgumentException("Target user doesn't exist.");
        }

        // Check if user is admin
        if (response.data().getRole() == UserRole.ADMIN) {
            log.error("Admin user cannot be disabled.");
            throw new IllegalArgumentException("Admin user cannot be disabled.");
        }

        // Update user status
        this.lambdaUpdate().eq(User::getId, id).set(User::getStatus, status).update();

        log.debug("Update user status successfully.");
        return ServiceResponse.success("Update user status successfully.");
    }

    /**
     * Update user role
     */
    @Transactional
    public ServiceResponse<?> updateUserRole(Integer id, UserRole role) {
        // Check if user exists
        ServiceResponse<User> response = getUserById(id);
        if (!response.success()) {
            log.error("Target user {} doesn't exist.", id);
            throw new IllegalArgumentException("Target user doesn't exist.");
        }

        this.lambdaUpdate().eq(User::getId, id).set(User::getRole, role).update();

        // Check number of admin user after this transaction
        Long adminNumber = this.lambdaQuery().eq(User::getStatus, UserRole.ADMIN).count();
        if (adminNumber < 1) {
            log.error("Number of admin user should be at least 1.");
            throw new IllegalArgumentException("Number of admin user should be at least 1.");
        } else if (adminNumber > 5) {
            log.error("Number of admin user should be at most 5.");
            throw new IllegalArgumentException("Number of admin user should be at most 5.");
        }

        log.debug("Update user role successfully.");
        return ServiceResponse.success("Update user role successfully.");
    }
}

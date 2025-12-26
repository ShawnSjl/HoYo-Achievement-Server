package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
import tech.sjiale.hoyo_achievement_server.dto.user_request.UserExposeDto;
import tech.sjiale.hoyo_achievement_server.entity.Account;
import tech.sjiale.hoyo_achievement_server.entity.User;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserRole;
import tech.sjiale.hoyo_achievement_server.entity.nume.UserStatus;
import tech.sjiale.hoyo_achievement_server.mapper.UserMapper;

import java.util.List;

@Slf4j
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final AccountService accountService;

    /**
     * Get user by id
     *
     * @param id user id
     * @return ServiceResponse with a User object
     */
    public ServiceResponse<User> getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            return ServiceResponse.error("User id doesn't exist: " + id);
        }
        return ServiceResponse.success("Get user successfully by id: " + id, user);
    }

    /**
     * Get user by username
     *
     * @param name username
     * @return ServiceResponse with a User object
     */
    public ServiceResponse<User> getUserByName(String name) {
        User user = this.lambdaQuery().eq(User::getUsername, name).one();
        if (user == null) {
            return ServiceResponse.error("User name doesn't exist: " + name);
        }
        return ServiceResponse.success("Get user by username successfully: " + name, user);
    }

    /**
     * Get all users; should only be called by admin or root
     *
     * @return ServiceResponse with a list of User objects
     */
    public ServiceResponse<List<UserExposeDto>> getAllUsers() {
        List<UserExposeDto> users = this.baseMapper.selectAll();
        if (users == null || users.isEmpty()) {
            // There should always be at least one user that is the root user
            return ServiceResponse.error("No user found.");
        }
        return ServiceResponse.success("Get all users successfully.", users);
    }

    /**
     * Create a new user; should only be called by admin or root
     *
     * @param username username
     * @param password password
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> createUser(String username, String password) {
        // Reserve the root username
        if (username.equals("root")) {
            return ServiceResponse.error("Root username cannot be created.");
        }

        // Check if the username already exists
        ServiceResponse<User> response = getUserByName(username);
        if (response.success()) {
            return ServiceResponse.error("Username already exists: " + username);
        }

        // Create a new user
        User user = new User();
        user.setUsername(username);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        // Save the new user
        boolean success = this.save(user);
        if (!success) {
            throw new RuntimeException("Create user failed: " + username);
        }
        return ServiceResponse.success("Create user successfully: ", username);
    }

    /**
     * Update username; should only be called by the user itself
     *
     * @param id          user id
     * @param newUsername new username
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateUsername(Long id, String newUsername) {
        // Reserve the root username
        if (newUsername.equals("root")) {
            return ServiceResponse.error("Root username cannot be created.");
        }

        // Check if the username already exists
        ServiceResponse<User> response = getUserByName(newUsername);
        if (response.success()) {
            return ServiceResponse.error("Username already exists: " + newUsername);
        }

        // Update username
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getUsername, newUsername)
                .update();
        if (!updated) {
            throw new RuntimeException("Update username failed: " + newUsername);
        }
        return ServiceResponse.success("Update username successfully: ", newUsername);
    }

    /**
     * Update user password; should only be called by the user itself
     *
     * @param id          user id
     * @param newPassword new password
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updatePassword(Long id, String newPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Update password
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getPassword, hashedPassword)
                .update();
        if (!updated) {
            throw new RuntimeException("Update password failed: " + id);
        }
        return ServiceResponse.success("Update password successfully: " + id);
    }

    /**
     * Update user status, cannot disable an admin or root account; should only be called by admin and root
     *
     * @param id     user id
     * @param status new status
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateUserStatus(Long id, UserStatus status) {
        // Check if the user exists
        ServiceResponse<User> response = getUserById(id);
        if (!response.success()) {
            return ServiceResponse.error("Target user doesn't exist, update status failed: " + id);
        }

        // Check if the user is admin or root
        if (response.data().getRole() == UserRole.ADMIN || response.data().getRole() == UserRole.ROOT) {
            return ServiceResponse.error("Admin and root user cannot be disabled: " + id);
        }

        // Update user status
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getStatus, status)
                .update();
        if (!updated) {
            throw new RuntimeException("Update user status failed: " + id);
        }
        return ServiceResponse.success("Update user status successfully: " + id);
    }

    /**
     * Update user role;
     * Should only be called by admin or root;
     * Root cannot be assigned to another role or changed to another role;
     * Admin user should not exceed 5.
     *
     * @param id   user id
     * @param role new role
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateUserRole(Long id, UserRole role) {
        // Cannot set to root
        if (role == UserRole.ROOT) {
            return ServiceResponse.error("Root cannot be assigned to a user.");
        }

        // Check if the user exists
        ServiceResponse<User> response = getUserById(id);
        if (!response.success()) {
            return ServiceResponse.error("Target user doesn't exist, update status failed: " + id);
        }

        // Cannot change the role of a root user
        if (response.data().getRole() == UserRole.ROOT) {
            return ServiceResponse.error("Root cannot be changed to another role.");
        }

        // Check the number of admin users before this transaction
        Long adminNumber = this.lambdaQuery().eq(User::getStatus, UserRole.ADMIN).count();
        if (adminNumber >= 5 && role == UserRole.ADMIN) {
            return ServiceResponse.error("Number of admin user should be at most 5.");
        }

        // Update user role
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getRole, role)
                .update();
        if (!updated) {
            throw new RuntimeException("Update user role failed: " + id);
        }
        return ServiceResponse.success("Update user role successfully: " + id);
    }

    /**
     * Delete user, cannot delete root user; should only be called by the user itself
     *
     * @param id user id
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> deleteUser(Long id) {
        // Cannot delete root user
        User user = this.getById(id);
        if (user != null && user.getRole() == UserRole.ROOT) {
            return ServiceResponse.error("Root user cannot be deleted.");
        }

        // Delete all accounts associated with the user
        ServiceResponse<List<Account>> response = accountService.getAllAccountsByUserId(id);
        if (!response.success()) {
            log.error(response.message());
        } else {
            for (Account account : response.data()) {
                accountService.deleteAccount(account.getAccountUuid());
            }
        }

        // Delete user
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new RuntimeException("Delete user failed: " + id);
        }
        return ServiceResponse.success("Delete user successfully: " + id);
    }

    /**
     * Create a root user; should only be called by the system initialization script
     *
     * @param username username
     * @param password password
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> createRootUser(String username, String password) {
        // Check if the root already exists
        Long rootCount = this.lambdaQuery().eq(User::getRole, UserRole.ROOT).count();
        if (rootCount != 0) {
            return ServiceResponse.success("Root already exists.");
        }

        // Check if the username already exists
        ServiceResponse<User> response = getUserByName(username);
        if (response.success()) {
            return ServiceResponse.error("Root username already exists: " + username);
        }

        // Create a new root user
        User root = new User();
        root.setUsername(username);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        root.setPassword(hashedPassword);

        root.setRole(UserRole.ROOT);
        root.setStatus(UserStatus.ACTIVE);

        // Save the new root user
        boolean success = this.save(root);
        if (success) {
            log.debug("Create root user {} successfully.", username);
            return ServiceResponse.success("Create root user successfully.", root);
        } else {
            log.error("Create root user {} failed.", username);
            throw new RuntimeException("Create root user failed.");
        }
    }
}

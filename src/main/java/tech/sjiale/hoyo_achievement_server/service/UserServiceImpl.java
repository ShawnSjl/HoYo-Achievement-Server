package tech.sjiale.hoyo_achievement_server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sjiale.hoyo_achievement_server.dto.ServiceResponse;
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
            log.error("User id {} doesn't exist.", id);
            return ServiceResponse.error("User id doesn't exist.");
        }
        log.debug("Get user by id {} successfully.", id);
        return ServiceResponse.success("Get user by id successfully.", user);
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
            log.error("User name {} doesn't exist.", name);
            return ServiceResponse.error("User name doesn't exist.");
        }
        log.debug("Get user by username {} successfully.", name);
        return ServiceResponse.success("Get user by username successfully.", user);
    }

    /**
     * Get all users; should only be called by admin or root
     *
     * @return ServiceResponse with a list of User objects
     */
    public ServiceResponse<List<User>> getAllUsers() {
        List<User> users = this.list();
        if (users == null || users.isEmpty()) {
            log.error("No user found.");
            return ServiceResponse.error("No user found.");
        }
        log.debug("Get all users successfully.");
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
        // Check if the username already exists
        ServiceResponse<User> response = getUserByName(username);
        if (response.success()) {
            log.error("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists.");
        }

        // Create a new user
        User user = new User();
        user.setUsername(username);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        // Save the new user
        boolean success = this.save(user);
        if (success) {
            log.debug("Create user {} successfully.", username);
            return ServiceResponse.success("Create user successfully.", user);
        } else {
            log.error("Create user {} failed.", username);
            throw new RuntimeException("Create user failed.");
        }
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
        // Check if the username already exists
        ServiceResponse<User> response = getUserByName(newUsername);
        if (response.success()) {
            log.error("New username already exists: {}", newUsername);
            throw new IllegalArgumentException("New username already exists.");
        }

        // Update username
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getUsername, newUsername)
                .update();
        if (updated) {
            log.debug("Update username {} successfully.", newUsername);
            return ServiceResponse.success("Update username successfully.");
        } else {
            log.error("Update username {} failed.", newUsername);
            throw new RuntimeException("Update username failed.");
        }
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
        if (updated) {
            log.debug("Update password successfully.");
            return ServiceResponse.success("Update password successfully.");
        } else {
            log.error("Update password failed.");
            throw new RuntimeException("Update password failed.");
        }
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
            log.error("Target user {} doesn't exist, update status failed.", id);
            throw new IllegalArgumentException("Target user doesn't exist, update status failed.");
        }

        // Check if the user is admin or root
        if (response.data().getRole() == UserRole.ADMIN || response.data().getRole() == UserRole.ROOT) {
            log.error("Admin or root user cannot be disabled.");
            throw new IllegalArgumentException("Admin or root user cannot be disabled.");
        }

        // Update user status
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getStatus, status)
                .update();
        if (updated) {
            log.debug("Update user status successfully.");
            return ServiceResponse.success("Update user status successfully.");
        } else {
            log.error("Update user status failed.");
            throw new RuntimeException("Update user status failed.");
        }
    }

    /**
     * Update user role; should only be called by admin or root
     *
     * @param id   user id
     * @param role new role
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse<?> updateUserRole(Long id, UserRole role) {
        // Cannot set to root
        if (role == UserRole.ROOT) {
            log.error("Root cannot be assigned to user.");
            throw new IllegalArgumentException("Root cannot be assigned to user.");
        }

        // Check if the user exists
        ServiceResponse<User> response = getUserById(id);
        if (!response.success()) {
            log.error("Target user {} doesn't exist, update role failed.", id);
            throw new IllegalArgumentException("Target user doesn't exist, update role failed.");
        }

        // Update user role
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getRole, role)
                .update();
        if (!updated) {
            log.error("Update user role failed.");
            throw new RuntimeException("Update user role failed.");
        }

        // Check the number of admin users after this transaction
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
            log.error("Root user cannot be deleted.");
            throw new IllegalArgumentException("Root user cannot be deleted.");
        }

        // Delete all accounts associated with the user
        ServiceResponse<List<Account>> response = accountService.getAllAccountsByUserId(id);
        if (!response.success()) {
            log.debug("No account found for user id: {}.", id);
        } else {
            for (Account account : response.data()) {
                accountService.deleteAccount(account.getAccount_uuid());
            }
        }

        // Delete user
        boolean removed = this.removeById(id);
        if (removed) {
            log.debug("Delete user {} successfully.", id);
            return ServiceResponse.success("Delete user successfully.");
        } else {
            log.error("Delete user {} failed.", id);
            throw new RuntimeException("Delete user failed.");
        }
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
        ServiceResponse<User> response = getUserByName(username);
        if (response.success()) {
            log.debug("Root already exists: {}", username);
            return ServiceResponse.success("Root already exists.");
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

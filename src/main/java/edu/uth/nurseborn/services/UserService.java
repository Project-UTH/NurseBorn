package edu.uth.nurseborn.services;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Tạo user mới
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        return userRepository.save(user);
    }

    // Lấy user theo ID
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    // Lấy user theo username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Lấy tất cả user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Lấy user theo role
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Lấy user theo email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Cập nhật user
    public User updateUser(Integer userId, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(userId);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        User existingUser = existingUserOpt.get();

        // Kiểm tra username và email duy nhất (nếu thay đổi)
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại");
        }
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Cập nhật các field
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPasswordHash(updatedUser.getPasswordHash());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setVerified(updatedUser.getVerified());

        return userRepository.save(existingUser);
    }

    // Xóa user
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
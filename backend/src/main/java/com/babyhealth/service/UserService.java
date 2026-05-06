package com.babyhealth.service;

import com.babyhealth.dto.LoginRequest;
import com.babyhealth.dto.RegisterRequest;
import com.babyhealth.entity.User;
import com.babyhealth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setBabyAge(request.getBabyAge());
        user.setBabyGender(request.getBabyGender());
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getRole() != null && user.getRole() == 2;
    }

    public User updateUser(Long id, RegisterRequest request) {
        User user = getUserById(id);
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBabyAge() != null) user.setBabyAge(request.getBabyAge());
        if (request.getBabyGender() != null) user.setBabyGender(request.getBabyGender());
        return userRepository.save(user);
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id, Long currentUserId) {
        if (!isAdmin(currentUserId)) {
            throw new RuntimeException("无权删除用户");
        }
        if (id.equals(currentUserId)) {
            throw new RuntimeException("不能删除自己");
        }
        userRepository.deleteById(id);
    }

    public User updateUserPassword(Long id, String newPassword, Long currentUserId) {
        if (!isAdmin(currentUserId)) {
            throw new RuntimeException("无权修改密码");
        }
        User user = getUserById(id);
        user.setPassword(newPassword);
        return userRepository.save(user);
    }

    public User updateUserRole(Long id, Integer role, Long currentUserId) {
        if (!isAdmin(currentUserId)) {
            throw new RuntimeException("无权修改角色");
        }
        if (id.equals(currentUserId)) {
            throw new RuntimeException("不能修改自己的角色");
        }
        User user = getUserById(id);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User updateUserAvatar(Long id, String avatar, Long currentUserId) {
        if (!isAdmin(currentUserId) && !id.equals(currentUserId)) {
            throw new RuntimeException("无权修改头像");
        }
        User user = getUserById(id);
        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    public Map<String, Object> getUserStatistics() {
        List<User> allUsers = userRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = allUsers.size();
        stats.put("totalUsers", totalUsers);

        Map<String, Long> genderStats = new HashMap<>();
        genderStats.put("男", allUsers.stream().filter(u -> "男".equals(u.getBabyGender())).count());
        genderStats.put("女", allUsers.stream().filter(u -> "女".equals(u.getBabyGender())).count());
        genderStats.put("未知", totalUsers - genderStats.get("男") - genderStats.get("女"));
        stats.put("genderDistribution", genderStats);

        Map<String, Long> ageGroups = new HashMap<>();
        ageGroups.put("0-6个月", allUsers.stream().filter(u -> {
            try { int age = Integer.parseInt(u.getBabyAge()); return age >= 0 && age <= 6; } catch (Exception e) { return false; }
        }).count());
        ageGroups.put("7-12个月", allUsers.stream().filter(u -> {
            try { int age = Integer.parseInt(u.getBabyAge()); return age >= 7 && age <= 12; } catch (Exception e) { return false; }
        }).count());
        ageGroups.put("13-24个月", allUsers.stream().filter(u -> {
            try { int age = Integer.parseInt(u.getBabyAge()); return age >= 13 && age <= 24; } catch (Exception e) { return false; }
        }).count());
        ageGroups.put("25-36个月", allUsers.stream().filter(u -> {
            try { int age = Integer.parseInt(u.getBabyAge()); return age >= 25 && age <= 36; } catch (Exception e) { return false; }
        }).count());
        ageGroups.put("36个月以上", allUsers.stream().filter(u -> {
            try { int age = Integer.parseInt(u.getBabyAge()); return age > 36; } catch (Exception e) { return false; }
        }).count());
        stats.put("ageDistribution", ageGroups);

        long adminCount = allUsers.stream().filter(u -> u.getRole() != null && u.getRole() == 2).count();
        stats.put("adminCount", adminCount);
        stats.put("regularUserCount", totalUsers - adminCount);

        return stats;
    }
}

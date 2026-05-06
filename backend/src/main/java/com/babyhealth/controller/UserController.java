package com.babyhealth.controller;

import com.babyhealth.dto.ApiResponse;
import com.babyhealth.dto.RegisterRequest;
import com.babyhealth.entity.User;
import com.babyhealth.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getUserInfo(@PathVariable Long id) {
        User user = userService.getUserById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("babyAge", user.getBabyAge());
        data.put("babyGender", user.getBabyGender());
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable Long id,
                                                        @RequestBody RegisterRequest request) {
        User user = userService.updateUser(id, request);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("nickname", user.getNickname());
        data.put("babyAge", user.getBabyAge());
        data.put("babyGender", user.getBabyGender());
        return ApiResponse.success("更新成功", data);
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> listUsers() {
        List<User> users = userService.listAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("phone", user.getPhone());
            data.put("babyAge", user.getBabyAge());
            data.put("babyGender", user.getBabyGender());
            data.put("avatar", user.getAvatar());
            data.put("role", user.getRole());
            result.add(data);
        }
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id, @RequestParam Long currentUserId) {
        userService.deleteUser(id, currentUserId);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> updatePassword(@PathVariable Long id,
                                             @RequestBody Map<String, String> body,
                                             @RequestParam Long currentUserId) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ApiResponse.error(400, "密码不能为空");
        }
        userService.updateUserPassword(id, newPassword, currentUserId);
        return ApiResponse.success("密码修改成功", null);
    }

    @PutMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id,
                                         @RequestBody Map<String, Integer> body,
                                         @RequestParam Long currentUserId) {
        Integer role = body.get("role");
        if (role == null || (role != 1 && role != 2)) {
            return ApiResponse.error(400, "角色值无效");
        }
        userService.updateUserRole(id, role, currentUserId);
        return ApiResponse.success("角色修改成功", null);
    }

    @PutMapping("/{id}/avatar")
    public ApiResponse<Void> updateAvatar(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          @RequestParam Long currentUserId) {
        String avatar = body.get("avatar");
        if (avatar == null || avatar.isEmpty()) {
            return ApiResponse.error(400, "头像不能为空");
        }
        userService.updateUserAvatar(id, avatar, currentUserId);
        return ApiResponse.success("头像更新成功", null);
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = userService.getUserStatistics();
        return ApiResponse.success(stats);
    }
}

package com.babyhealth.controller;

import com.babyhealth.dto.ApiResponse;
import com.babyhealth.dto.LoginRequest;
import com.babyhealth.dto.RegisterRequest;
import com.babyhealth.entity.User;
import com.babyhealth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        return ApiResponse.success("注册成功", data);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("babyAge", user.getBabyAge());
        data.put("babyGender", user.getBabyGender());
        data.put("role", user.getRole());
        return ApiResponse.success("登录成功", data);
    }
}

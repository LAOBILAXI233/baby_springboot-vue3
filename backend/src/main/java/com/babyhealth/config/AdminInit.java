package com.babyhealth.config;

import com.babyhealth.entity.User;
import com.babyhealth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInit {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin");
                admin.setNickname("管理员");
                admin.setRole(2);
                userRepository.save(admin);
                System.out.println("[AdminInit] 管理员账户已创建: admin / admin");
            }
        };
    }
}

package com.eugene.goalhub.admin.init;


import com.eugene.goalhub.admin.entity.AdminUser;
import com.eugene.goalhub.admin.mapper.AdminUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserMapper adminUserMapper,PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.adminUserMapper = adminUserMapper;
        // this.jwtUtil = jwtUtil;
    }
    @Override
    public void run(String... args) {
        Long count = adminUserMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        AdminUser user = new AdminUser();
        user.setUsername("eugene");
        user.setPasswordHash(passwordEncoder.encode("eugene"));
        user.setNickname("超级管理员");
        user.setIsSuperAdmin(1);
        user.setStatus(1);
        user.setDeleted(0);

        adminUserMapper.insert(user);
    }
}
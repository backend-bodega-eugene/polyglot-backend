package com.eugene.goalhub.admin.init;


import com.eugene.goalhub.admin.entity.AdminUser;
import com.eugene.goalhub.admin.mapper.AdminUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 后台管理员初始化器。
 * <p>
 * 应用启动时如果管理员表为空，则创建默认超级管理员账号。
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    /**
     * 管理员账号 Mapper。
     */
    private final AdminUserMapper adminUserMapper;

    /**
     * 密码加密器。
     */
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserMapper adminUserMapper,PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.adminUserMapper = adminUserMapper;
        // this.jwtUtil = jwtUtil;
    }

    /**
     * 启动后执行默认管理员初始化逻辑。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        Long count = adminUserMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        // 首次启动时写入一个可登录的超级管理员账号。
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

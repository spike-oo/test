package com.campus.delivery.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.delivery.common.exception.BusinessException;
import com.campus.delivery.common.util.PasswordGenerator;
import com.campus.delivery.modules.user.dto.LoginRequest;
import com.campus.delivery.modules.user.dto.LoginResponse;
import com.campus.delivery.modules.user.dto.RegisterRequest;
import com.campus.delivery.modules.user.entity.DietProfile;
import com.campus.delivery.modules.user.entity.Student;
import com.campus.delivery.modules.user.entity.SysUser;
import com.campus.delivery.modules.user.mapper.DietProfileMapper;
import com.campus.delivery.modules.user.mapper.StudentMapper;
import com.campus.delivery.modules.user.mapper.SysUserMapper;
import com.campus.delivery.security.JwtUtils;
import com.campus.delivery.security.LoginUser;
import com.campus.delivery.security.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务：登录、注册、退出。主责：成员4。
 *
 * <p>安全要点（需求文档 5.6.1）：
 * <ul>
 *   <li>密码 BCrypt 校验，账号或密码错误统一返回同一提示，避免账号枚举；</li>
 *   <li>账号被禁用不允许登录；</li>
 *   <li>登录入口与账号角色不匹配时拒绝登录。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 业务码段：1xxxx 用户与权限 */
    private static final int CODE_LOGIN_FAILED = 10001;
    private static final int CODE_ACCOUNT_DISABLED = 10002;
    private static final int CODE_ROLE_MISMATCH = 10003;
    private static final int CODE_USERNAME_EXISTS = 10004;

    private final SysUserMapper sysUserMapper;
    private final StudentMapper studentMapper;
    private final DietProfileMapper dietProfileMapper;
    private final JwtUtils jwtUtils;

    /** 登录 */
    public LoginResponse login(LoginRequest request) {
        SysUser user = findByUsername(request.getUsername());
        if (user == null || !PasswordGenerator.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(CODE_LOGIN_FAILED, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(CODE_ACCOUNT_DISABLED, "账号已被禁用，请联系管理员");
        }
        if (StringUtils.hasText(request.getRole())
                && !request.getRole().equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(CODE_ROLE_MISMATCH, "账号角色与当前登录入口不匹配");
        }

        // 记录最近登录时间
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(update);

        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole(), user.getNickname());
        String token = jwtUtils.generateToken(loginUser);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        response.setExpiresIn(jwtUtils.getExpireMinutes() * 60);
        log.info("用户登录成功: userId={}, role={}", user.getId(), user.getRole());
        return response;
    }

    /** 学生注册：同时创建账号、学生档案与空白饮食档案 */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        if (findByUsername(request.getStudentNo()) != null) {
            throw new BusinessException(CODE_USERNAME_EXISTS, "该学号已注册");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getStudentNo());
        user.setPassword(PasswordGenerator.encode(request.getPassword()));
        user.setRole(RoleEnum.STUDENT.getCode());
        user.setNickname(StringUtils.hasText(request.getRealName()) ? request.getRealName() : request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        sysUserMapper.insert(user);

        Student student = new Student();
        student.setUserId(user.getId());
        student.setStudentNo(request.getStudentNo());
        student.setRealName(request.getRealName());
        student.setCollege(request.getCollege());
        student.setGrade(request.getGrade());
        studentMapper.insert(student);

        // 初始化空白饮食档案，保证后续推荐与饮食分析有画像载体
        DietProfile profile = new DietProfile();
        profile.setUserId(user.getId());
        profile.setDietGoal(0);
        dietProfileMapper.insert(profile);

        log.info("学生注册成功: userId={}, studentNo={}", user.getId(), request.getStudentNo());
    }

    /**
     * 退出登录。
     *
     * <p>JWT 无状态，服务端不做会话销毁。如需强制失效，可把 Token 写入
     * {@code RedisKeys.TOKEN_BLACKLIST} 并在拦截器中校验（预留实现）。
     */
    public void logout(String token) {
        // TODO(成员4): 将 token 写入 Redis 黑名单，过期时间取 Token 剩余有效期
        log.info("用户退出登录");
    }

    private SysUser findByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
    }
}

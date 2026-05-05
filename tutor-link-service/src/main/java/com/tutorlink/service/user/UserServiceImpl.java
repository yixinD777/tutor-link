package com.tutorlink.service.user;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.constant.UserRole;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.JwtUtil;
import com.tutorlink.common.util.RedisKeyUtil;
import com.tutorlink.dao.mapper.UserMapper;
import com.tutorlink.dao.mapper.UserProfileMapper;
import com.tutorlink.model.dto.user.LoginResponse;
import com.tutorlink.model.dto.user.PasswordLoginRequest;
import com.tutorlink.model.dto.user.PhoneLoginRequest;
import com.tutorlink.model.dto.user.RegisterRequest;
import com.tutorlink.model.dto.user.WxLoginRequest;
import com.tutorlink.model.entity.User;
import com.tutorlink.model.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Value("${wechat.miniapp.appid:}")
    private String wxAppId;

    @Value("${wechat.miniapp.secret:}")
    private String wxSecret;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查账号是否已存在
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getAccount, request.getAccount()));
        if (existing != null) {
            throw new BusinessException(ResultCode.ACCOUNT_ALREADY_EXISTS);
        }

        // 校验角色：只允许家长(1)或学生(2)
        int role = request.getRole();
        if (role != UserRole.PARENT.getCode() && role != UserRole.TUTOR.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色值无效，仅支持家长(1)或学生(2)");
        }

        // 创建用户
        User user = new User();
        user.setAccount(request.getAccount());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getAccount());
        user.setRole(role);
        user.setStatus(1);
        user.setGender(0);
        userMapper.insert(user);

        // 创建空 profile
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        userProfileMapper.insert(profile);

        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse passwordLogin(PasswordLoginRequest request) {
        // 根据账号查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getAccount, request.getAccount()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (user.getPassword() == null || !PASSWORD_ENCODER.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.INVALID_PASSWORD);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        updateLastLogin(user.getId(), null);
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse phoneLogin(PhoneLoginRequest request) {
        // 1. 验证短信验证码
        String redisKey = RedisKeyUtil.smsCode(request.getPhone());
        String cachedCode = redisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null || !cachedCode.equals(request.getSmsCode())) {
            throw new BusinessException(ResultCode.INVALID_SMS_CODE);
        }
        redisTemplate.delete(redisKey);

        // 2. 查找或创建用户
        String phoneHash = DigestUtil.sha256Hex(request.getPhone());
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhoneHash, phoneHash));

        if (user == null) {
            user = createPhoneUser(request.getPhone(), phoneHash);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        updateLastLogin(user.getId(), null);
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse wxLogin(WxLoginRequest request) {
        // 1. 用 code 换取 openid + session_key
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxAppId, wxSecret, request.getCode());

        RestTemplate restTemplate = new RestTemplate();
        var response = restTemplate.getForObject(url, WxCode2SessionResponse.class);

        if (response == null || response.getOpenid() == null) {
            throw new BusinessException(ResultCode.WX_LOGIN_FAILED);
        }

        String openid = response.getOpenid();
        String sessionKey = response.getSessionKey();

        // 2. 缓存 session_key
        redisTemplate.opsForValue().set(
                RedisKeyUtil.wxSessionKey(openid), sessionKey, 30, TimeUnit.MINUTES);

        // 3. 查找或创建用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getWxOpenid, openid));

        if (user == null) {
            user = createWxUser(openid, response.getUnionid());
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        updateLastLogin(user.getId(), null);
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Refresh token 已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String cachedToken = redisTemplate.opsForValue().get(RedisKeyUtil.refreshToken(userId));

        if (cachedToken == null || !cachedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Refresh token 无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        return buildLoginResponse(user);
    }

    @Override
    public void logout(Long userId, String jti) {
        // 黑名单 access token
        if (jti != null) {
            redisTemplate.opsForValue().set(
                    RedisKeyUtil.tokenBlacklist(jti), "1",
                    accessTokenExpiry, TimeUnit.MILLISECONDS);
        }
        // 删除 refresh token
        redisTemplate.delete(RedisKeyUtil.refreshToken(userId));
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User getUserByPhone(String phone) {
        String phoneHash = DigestUtil.sha256Hex(phone);
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhoneHash, phoneHash));
    }

    @Override
    public void updateLastLogin(Long userId, String ip) {
        User update = new User();
        update.setId(userId);
        update.setLastLoginTime(LocalDateTime.now());
        update.setLastLoginIp(ip);
        userMapper.updateById(update);
    }

    // ==================== 私有方法 ====================

    private User createPhoneUser(String phone, String phoneHash) {
        User user = new User();
        user.setPhone(phone);
        user.setPhoneHash(phoneHash);
        user.setNickname("用户" + System.currentTimeMillis() % 100000);
        user.setRole(UserRole.PARENT.getCode());
        user.setStatus(1);
        user.setGender(0);
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        userProfileMapper.insert(profile);

        return user;
    }

    private User createWxUser(String openid, String unionid) {
        User user = new User();
        user.setWxOpenid(openid);
        user.setWxUnionid(unionid);
        user.setNickname("微信用户" + System.currentTimeMillis() % 100000);
        user.setRole(UserRole.PARENT.getCode());
        user.setStatus(1);
        user.setGender(0);
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        userProfileMapper.insert(profile);

        return user;
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 缓存 refresh token
        redisTemplate.opsForValue().set(
                RedisKeyUtil.refreshToken(user.getId()), refreshToken,
                refreshTokenExpiry, TimeUnit.MILLISECONDS);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    // 微信 code2session 响应 DTO (内部类)
    @lombok.Data
    private static class WxCode2SessionResponse {
        private String openid;
        private String sessionKey;
        private String unionid;
        private Integer errcode;
        private String errmsg;
    }
}

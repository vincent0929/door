package com.vc.door.core.service.impl;

import com.vc.door.core.constant.AppStatusEnum;
import com.vc.door.core.constant.BizErrorEnum;
import com.vc.door.core.constant.DoorConstants;
import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.entity.AccountDO;
import com.vc.door.core.entity.AppDO;
import com.vc.door.core.entity.UserDO;
import com.vc.door.core.manager.AccountManager;
import com.vc.door.core.manager.AppManager;
import com.vc.door.core.manager.LoginLogManager;
import com.vc.door.core.manager.UserManager;
import com.vc.door.core.service.LogService;
import com.vc.door.core.service.LoginService;
import io.github.vincent0929.common.util.BizAssert;
import io.github.vincent0929.common.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.vc.door.core.constant.BizErrorEnum.USER_NOT_LOGIN;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private AppManager appManager;

    @Autowired
    private LogService logService;

    @Override
    public boolean validToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        String value = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        return StringUtils.isNotBlank(value);
    }

    @Override
    public Long validTicket(String ticket, String appName, String appKey, String appSecret) {
        validApp(appName, appKey, appSecret);

        BizAssert.notBlank(ticket, BizErrorEnum.TICKET_ERROR);
        String token = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TICKET + ticket);
        BizAssert.isTrue(validToken(token), BizErrorEnum.TICKET_ERROR);
        redisTemplate.delete(DoorConstants.REDIS_PREFIX_TICKET + ticket);
        String userId = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        BizAssert.notBlank(userId, BizErrorEnum.TICKET_ERROR);
        return Long.valueOf(userId);
    }

    private void validApp(String appName, String appKey, String appSecret) {
        BizAssert.notBlank(appKey, BizErrorEnum.APP_KEY_ERROR);
        BizAssert.notBlank(appSecret, BizErrorEnum.APP_KEY_ERROR);
        AppDO appDO = appManager.getByName(appName);
        BizAssert.notNull(appDO, BizErrorEnum.APP_NOT_EXIST);
        BizAssert.isTrue(AppStatusEnum.ENABLE.getCode().equals(appDO.getStatus()), BizErrorEnum.APP_NOT_ENABLE);
        BizAssert.isTrue(appKey.equals(appDO.getAppKey())
                && appSecret.equals(appDO.getAppSecret()), BizErrorEnum.APP_KEY_ERROR);
    }

    @Override
    public String login(String identifier, String credential, IdentityTypeEnum identityType) {
        BizAssert.isTrue(StringUtils.isNotBlank(identifier) && StringUtils.isNotBlank(credential),
                BizErrorEnum.ACCOUNT_ERROR);
        String encryptCredential = DigestUtils.md5DigestAsHex(credential.getBytes(StandardCharsets.UTF_8));
        AccountDO accountDO = accountManager.getByIdentifier(identifier, encryptCredential, identityType);
        BizAssert.notNull(accountDO, BizErrorEnum.ACCOUNT_ERROR);
        String token = Strings.uuid();
        redisTemplate.opsForValue().set(DoorConstants.REDIS_PREFIX_TOKEN + token, String.valueOf(accountDO.getUserId()), Duration.ofDays(1));
        return token;
    }

    @Override
    public UserDO getUser(String token) {
        String userIdStr = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        BizAssert.notBlank(userIdStr, USER_NOT_LOGIN);
        Long userId = Long.valueOf(userIdStr);
        return userManager.get(userId);
    }

    @Override
    public String grantTicket(String token) {
        String ticket = Strings.uuid();
        redisTemplate.opsForValue().set(DoorConstants.REDIS_PREFIX_TICKET + ticket, token, Duration.ofMinutes(10));
        return ticket;
    }

    @Override
    public void logoutByToken(String token) {
        String userId = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        BizAssert.notBlank(userId, USER_NOT_LOGIN);
        logout(token);
    }

    @Override
    public void logoutByAppToken(String appToken) {
        String token = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_APP_TOKEN + appToken);
        BizAssert.notBlank(token, USER_NOT_LOGIN);
        String userId = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        BizAssert.notBlank(userId, USER_NOT_LOGIN);
        Map<Object, Object> appTokenMap = redisTemplate.opsForHash().entries(DoorConstants.REDIS_PREFIX_ALL_APP_TOKEN + token);
        BizAssert.isTrue(MapUtils.isNotEmpty(appTokenMap) && appTokenMap.containsValue(appToken),
                USER_NOT_LOGIN);

        Map<String, String> otherAppTokenMap = new HashMap<>();
        appTokenMap.forEach((app, t) -> {
            if (t.equals(appToken)) {
                return;
            }
            otherAppTokenMap.put(String.valueOf(app), String.valueOf(t));
        });
        if (MapUtils.isNotEmpty(otherAppTokenMap)) {
            CountDownLatch countDownLatch = new CountDownLatch(otherAppTokenMap.keySet().size());
            otherAppTokenMap.forEach((app, t) -> CompletableFuture.runAsync(() -> {
                AppDO appDO = appManager.getByName(app);
                if (appDO == null) {
                    return;
                }

                String logoutUrl = appDO.getLogoutUrl();

            }));
            try {
                boolean timeout = countDownLatch.await(1, TimeUnit.MINUTES);
                if (!timeout) {
                    log.error("登出所有业务系统超时,token={}", token);
                    return;
                }
            } catch (InterruptedException e) {
                log.error("登出所有业务系统被中断,token=" + token, e);
            }
        }
        logout(token);
    }

    private void logout(String token) {
        redisTemplate.delete(DoorConstants.REDIS_PREFIX_APP_TOKEN + token);
        redisTemplate.delete(DoorConstants.REDIS_PREFIX_TOKEN + token);
    }
}

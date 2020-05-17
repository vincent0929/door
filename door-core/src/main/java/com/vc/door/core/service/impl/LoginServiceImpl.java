package com.vc.door.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.vc.door.core.constant.AppStatusEnum;
import com.vc.door.core.constant.BizErrorEnum;
import com.vc.door.core.constant.DoorConstants;
import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.entity.AccountDO;
import com.vc.door.core.entity.AppDO;
import com.vc.door.core.entity.UserDO;
import com.vc.door.core.manager.AccountManager;
import com.vc.door.core.manager.AppManager;
import com.vc.door.core.manager.UserManager;
import com.vc.door.core.service.LogService;
import com.vc.door.core.service.LoginService;
import io.github.vincent0929.common.dto.ResultDTO;
import io.github.vincent0929.common.util.BizAssert;
import io.github.vincent0929.common.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.util.stream.Collectors;

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
    public Long validTicket(String ticket, String appName, String appToken, String appKey, String appSecret) {
        validApp(appName, appKey, appSecret);

        BizAssert.notBlank(ticket, BizErrorEnum.TICKET_ERROR);
        String token = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TICKET + ticket);
        BizAssert.isTrue(validToken(token), BizErrorEnum.TICKET_ERROR);
        redisTemplate.delete(DoorConstants.REDIS_PREFIX_TICKET + ticket);
        String userId = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        BizAssert.notBlank(userId, BizErrorEnum.TICKET_ERROR);
        redisTemplate.opsForHash().put(DoorConstants.REDIS_PREFIX_ALL_APP_TOKEN + token, appName, appToken);
        redisTemplate.opsForValue().set(DoorConstants.REDIS_PREFIX_APP_TOKEN + appName + DoorConstants.KEY_COLON + appToken, token);
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
    public ResultDTO<String> login(String identifier, String credential, IdentityTypeEnum identityType) {
        if (StringUtils.isBlank(identifier) || StringUtils.isBlank(credential)) {
            return ResultDTO.fail(BizErrorEnum.ACCOUNT_ERROR.getCode(), BizErrorEnum.ACCOUNT_ERROR.getDesc());
        }
        String encryptCredential = DigestUtils.md5DigestAsHex(credential.getBytes(StandardCharsets.UTF_8));
        AccountDO accountDO = accountManager.getByIdentifier(identifier, encryptCredential, identityType);
        if (accountDO == null) {
            return ResultDTO.fail(BizErrorEnum.ACCOUNT_ERROR.getCode(), BizErrorEnum.ACCOUNT_ERROR.getDesc());
        }
        String token = Strings.uuid();
        redisTemplate.opsForValue().set(DoorConstants.REDIS_PREFIX_TOKEN + token, String.valueOf(accountDO.getUserId()), Duration.ofDays(1));
        return ResultDTO.success(token);
    }

    @Override
    public UserDO getUser(String token) {
        String userId = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        BizAssert.notBlank(userId, USER_NOT_LOGIN);
        return userManager.get(Long.valueOf(userId));
    }

    @Override
    public String grantTicket(String token) {
        String ticket = Strings.uuid();
        redisTemplate.opsForValue().set(DoorConstants.REDIS_PREFIX_TICKET + ticket, token, Duration.ofMinutes(10));
        return ticket;
    }

    @Override
    public void logoutByToken(String logoutAppName, String token) {
        String userId = redisTemplate.opsForValue().get(DoorConstants.REDIS_PREFIX_TOKEN + token);
        if (StringUtils.isBlank(userId)) {
            return;
        }
        Map<Object, Object> appTokenMap = redisTemplate.opsForHash().entries(DoorConstants.REDIS_PREFIX_ALL_APP_TOKEN + token);
        BizAssert.isTrue(MapUtils.isNotEmpty(appTokenMap) && appTokenMap.containsKey(logoutAppName), USER_NOT_LOGIN);

        redisTemplate.delete(DoorConstants.REDIS_PREFIX_ALL_APP_TOKEN + token);
        redisTemplate.delete(DoorConstants.REDIS_PREFIX_TOKEN + token);

        CountDownLatch countDownLatch = new CountDownLatch(appTokenMap.keySet().size());
        appTokenMap.forEach((appName, appToken) -> CompletableFuture.runAsync(() -> {
            AppDO appDO = appManager.getByName(String.valueOf(appName));
            if (appDO == null) {
                return;
            }

            if (!appName.equals(logoutAppName)) {
                HttpGet get = new HttpGet(appDO.getLogoutUrl());
                Map<String, String> cookieMap = new HashMap<>();
                cookieMap.put(DoorConstants.COOKIE_APP_TOKEN, String.valueOf(appToken));
                cookieMap.put(DoorConstants.COOKIE_TOKEN, token);
                get.setHeader(DoorConstants.COOKIE, cookieMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(";")));
                try (CloseableHttpClient httpClient = HttpClients.createDefault();
                     CloseableHttpResponse httpResponse = httpClient.execute(get)) {
                    if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                        log.error("调用登出接口异常,token={}", token);
                        return;
                    }
                    HttpEntity entity = httpResponse.getEntity();
                    String result = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                    if (result == null || result.isEmpty()) {
                        log.error("调用登出接口异常,token={}", token);
                        return;
                    }
                    ResultDTO<Boolean> resultDTO = JSON.parseObject(result, new TypeReference<ResultDTO<Boolean>>() {
                    });
                    if (!resultDTO.isSuccess() || !Boolean.TRUE.equals(resultDTO.getData())) {
                        log.error("调用登出接口失败,token={}", token);
                        return;
                    }
                } catch (Exception e) {
                    log.error("登出异常,token=" + token, e);
                    return;
                }
            }

            redisTemplate.delete(DoorConstants.REDIS_PREFIX_APP_TOKEN + appName + DoorConstants.KEY_COLON + appToken);
        }));
        try {
            boolean timeout = countDownLatch.await(1, TimeUnit.MINUTES);
            if (!timeout) {
                log.error("登出所有业务系统超时,token={}", token);
            }
        } catch (InterruptedException e) {
            log.error("登出所有业务系统被中断,token=" + token, e);
        }
    }
}

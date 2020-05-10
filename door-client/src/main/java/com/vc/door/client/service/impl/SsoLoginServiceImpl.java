package com.vc.door.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.vc.door.client.constant.SsoConstant;
import com.vc.door.client.dto.TicketValidResponse;
import com.vc.door.client.service.SsoLoginService;
import io.github.vincent0929.common.dto.ResultDTO;
import io.github.vincent0929.common.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SsoLoginServiceImpl implements SsoLoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${door.path.host}")
    private String host;

    @Value("${door.path.valid}")
    private String validPath;

    @Value("${door.path.logout}")
    private String logoutPath;

    @Value("${door.app}")
    private String app;

    @Override
    public boolean validToken(String token) {
        String value = redisTemplate.opsForValue().get(SsoConstant.COOKIE_TOKEN + ":" + token);
        return StringUtils.isNotBlank(value);
    }

    @Override
    public ResultDTO validTicket(String ticket) {
        Map<String, String> data = new HashMap<>(4);
        data.put(SsoConstant.TICKET, ticket);
        String token = Strings.uuid();
        data.put(SsoConstant.TOKEN, token);
        data.put(SsoConstant.APP, app);
        HttpGet request = new HttpGet(host + validPath + "?" + data.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&")));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                return ResultDTO.fail(400, "调用ticket验证接口异常");
            }
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            if (result == null || result.isEmpty()) {
                return ResultDTO.fail(400, "调用ticket验证接口异常");
            }
            ResultDTO<Long> resultDTO = JSON.parseObject(result, new TypeReference<ResultDTO<Long>>() {
            });
            if (!resultDTO.isSuccess()) {
                return ResultDTO.fail(400, "ticket错误");
            }
            Long userId = resultDTO.getData();
            TicketValidResponse validResponse = new TicketValidResponse();
            validResponse.setToken(token);
            validResponse.setUserId(userId);
            return ResultDTO.success(validResponse);
        } catch (IOException e) {
            log.error("ticket验证异常, url=" + host + validPath, e);
            return ResultDTO.fail(400, "ticket验证异常");
        }
    }

    @Override
    public ResultDTO logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie tokenCookie = Arrays.stream(cookies).filter(cookie -> SsoConstant.COOKIE_TOKEN.equals(cookie.getName()))
                .findFirst().orElse(null);
        if (tokenCookie == null) {
            return ResultDTO.success(true);
        }

        String token = tokenCookie.getValue();
        HttpGet get = new HttpGet(host + logoutPath + "?appToken=" + token);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(get)) {
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("调用登出接口异常,token={}", token);
                return ResultDTO.fail(400, "调用登出接口异常");
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            if (result == null || result.isEmpty()) {
                log.error("调用登出接口异常,token={}", token);
                return ResultDTO.fail(400, "调用登出接口异常");
            }
            ResultDTO<Long> resultDTO = JSON.parseObject(result, new TypeReference<ResultDTO<Long>>() {
            });
            if (!resultDTO.isSuccess()) {
                log.error("调用登出接口失败,token={}", token);
                return ResultDTO.fail(400, "调用登出接口失败");
            }
        } catch (Exception e) {
            log.error("登出异常,token=" + token, e);
            return ResultDTO.fail(400, "登出异常");
        }
        redisTemplate.delete(SsoConstant.COOKIE_TOKEN + ":" + token);
        tokenCookie.setValue(null);
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);
        return ResultDTO.success(true);
    }
}

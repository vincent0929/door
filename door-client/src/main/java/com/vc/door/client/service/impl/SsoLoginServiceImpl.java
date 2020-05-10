package com.vc.door.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vc.door.client.constant.SsoConstant;
import com.vc.door.client.dto.TicketValidResponse;
import com.vc.door.client.service.SsoLoginService;
import io.github.vincent0929.common.dto.ResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Value("${door.app.name}")
    private String appName;

    @Value("${door.app.key}")
    private String appKey;

    @Value("${door.app.secret}")
    private String appSecret;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean validToken(String token) {
        String value = redisTemplate.opsForValue().get(SsoConstant.COOKIE_TOKEN + ":" + token);
        return StringUtils.isNotBlank(value);
    }

    @Override
    public ResultDTO<TicketValidResponse> validTicket(String ticket) {
        Map<String, String> data = new HashMap<>(4);
        data.put(SsoConstant.TICKET, ticket);
        data.put(SsoConstant.APP_NAME, appName);
        data.put(SsoConstant.APP_KEY, appKey);
        data.put(SsoConstant.APP_SECRET, appSecret);
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
            validResponse.setUserId(userId);
            return ResultDTO.success(validResponse);
        } catch (IOException e) {
            log.error("ticket验证异常, url=" + host + validPath, e);
            return ResultDTO.fail(400, "ticket验证异常");
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isEmpty(cookies)) {
            returnResult(response, ResultDTO.fail(400, "登出异常"));
            return;
        }

        Cookie token = null;
        Cookie dToken = null;
        for (Cookie cookie : cookies) {
            if (SsoConstant.COOKIE_TOKEN.equals(cookie.getName())) {
                token = cookie;
            } else if (SsoConstant.D_TOKEN.equals(cookie.getName())) {
                dToken = cookie;
            }
        }
        if (token != null) {
            redisTemplate.delete(SsoConstant.COOKIE_TOKEN + ":" + token);
            token.setValue(null);
            token.setMaxAge(0);
            response.addCookie(token);
        }

        if (dToken == null) {
            returnResult(response, ResultDTO.fail(400, "用户未登录"));
            return;
        }
        String dTokenValue = dToken.getValue();
        dToken.setValue(null);
        dToken.setMaxAge(0);
        response.addCookie(dToken);

        HttpGet get = new HttpGet(host + logoutPath + "?" + SsoConstant.TOKEN + "=" + dTokenValue);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(get)) {
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("调用登出接口异常,token={}", token);
                returnResult(response, ResultDTO.fail(400, "调用登出接口异常"));
                return;
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            if (result == null || result.isEmpty()) {
                log.error("调用登出接口异常,token={}", token);
                returnResult(response, ResultDTO.fail(400, "调用登出接口异常"));
                return;
            }
            ResultDTO<Boolean> resultDTO = JSON.parseObject(result, new TypeReference<ResultDTO<Boolean>>() {
            });
            if (!resultDTO.isSuccess() || !Boolean.TRUE.equals(resultDTO.getData())) {
                log.error("调用登出接口失败,token={}", token);
                returnResult(response, ResultDTO.fail(400, "调用登出接口失败"));
                return;
            }
        } catch (Exception e) {
            log.error("登出异常,token=" + token, e);
            returnResult(response, ResultDTO.fail(400, "登出异常"));
            return;
        }
        returnResult(response, ResultDTO.success(true));
    }

    private void returnResult(HttpServletResponse response, ResultDTO<?> resultDTO) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(objectMapper.writeValueAsString(resultDTO).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }
}

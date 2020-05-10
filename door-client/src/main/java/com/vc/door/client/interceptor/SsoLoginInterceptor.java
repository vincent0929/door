package com.vc.door.client.interceptor;

import com.vc.door.client.constant.SsoConstant;
import com.vc.door.client.dto.TicketValidResponse;
import com.vc.door.client.service.SsoLoginService;
import io.github.vincent0929.common.dto.ResultDTO;
import io.github.vincent0929.common.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SsoLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private SsoLoginService loginService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${door.path.host}")
    private String host;

    @Value("${door.path.login}")
    private String loginPath;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String requestUrl = request.getRequestURL().toString();
        if (!parameterMap.isEmpty()) {
            requestUrl += "?" + parameterMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + (entry.getValue() != null ? entry.getValue()[0] : ""))
                    .collect(Collectors.joining("&"));
        }

        Cookie[] cookies = request.getCookies();
        String token = null;
        String ticket = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (SsoConstant.COOKIE_TOKEN.equals(cookie.getName())) {
                    token = cookie.getValue();
                } else if (SsoConstant.TICKET.equals(cookie.getName())) {
                    ticket = cookie.getValue();
                }
            }
        }
        if (token != null && token.length() > 0) {
            boolean valid = loginService.validToken(token);
            if (valid) {
                return true;
            }
        } else {
            if (ticket != null && ticket.length() > 0) {
                ResultDTO<TicketValidResponse> resultDTO = loginService.validTicket(ticket);
                if (resultDTO.isSuccess()) {
                    TicketValidResponse validResponse = resultDTO.getData();
                    token = Strings.uuid();
                    redisTemplate.opsForValue().set(SsoConstant.COOKIE_TOKEN + ":" + token, String.valueOf(validResponse.getUserId()), Duration.ofDays(1));
                    response.addCookie(new Cookie(SsoConstant.COOKIE_TOKEN, token));
                    response.sendRedirect(requestUrl);
                    return false;
                }
            }
        }
        response.sendRedirect(host + loginPath + "?" +SsoConstant.CALLBACK + "=" + URLEncoder.encode(requestUrl, StandardCharsets.UTF_8.displayName()));
        return false;
    }
}

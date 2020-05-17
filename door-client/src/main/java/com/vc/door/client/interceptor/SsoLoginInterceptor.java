package com.vc.door.client.interceptor;

import com.vc.door.client.constant.SsoConstant;
import com.vc.door.client.dto.TicketValidResponse;
import com.vc.door.client.service.SsoLoginService;
import io.github.vincent0929.common.dto.ResultDTO;
import org.apache.commons.lang3.StringUtils;
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
        Cookie tokenCookie = null;
        Cookie ticketCookie = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (SsoConstant.COOKIE_TOKEN.equals(cookie.getName())) {
                    tokenCookie = cookie;
                } else if (SsoConstant.TICKET.equals(cookie.getName())) {
                    ticketCookie = cookie;
                }
            }
        }
        if (tokenCookie != null && StringUtils.isNotBlank(tokenCookie.getValue())) {
            boolean valid = loginService.validToken(tokenCookie.getValue());
            if (valid) {
                return true;
            }
        } else {
            if (ticketCookie != null && StringUtils.isNotBlank(ticketCookie.getValue())) {
                ResultDTO<TicketValidResponse> resultDTO = loginService.validTicket(ticketCookie.getValue());
                ticketCookie.setValue(null);
                ticketCookie.setMaxAge(0);
                response.addCookie(ticketCookie);
                if (resultDTO.isSuccess()) {
                    TicketValidResponse validResponse = resultDTO.getData();
                    redisTemplate.opsForValue().set(SsoConstant.COOKIE_TOKEN + ":" + validResponse.getToken(),
                            String.valueOf(validResponse.getUserId()), Duration.ofDays(1));
                    response.addCookie(new Cookie(SsoConstant.COOKIE_TOKEN, validResponse.getToken()));
                    response.sendRedirect(requestUrl);
                    return false;
                }
            }
        }
        response.sendRedirect(host + loginPath + "?" + SsoConstant.CALLBACK + "=" + URLEncoder.encode(requestUrl, StandardCharsets.UTF_8.displayName()));
        return false;
    }
}

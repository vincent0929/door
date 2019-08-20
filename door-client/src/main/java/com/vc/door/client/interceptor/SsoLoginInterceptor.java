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
import java.time.Duration;

@Component
public class SsoLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private SsoLoginService loginService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${door.login.url}")
    private String loginUrl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (SsoConstant.COOKIE_TOKEN.equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        boolean valid = false;
        if (token != null && token.length() > 0) {
            valid = loginService.validToken(token);
        } else {
            String ticket = request.getParameter(SsoConstant.TICKET);
            if (ticket != null && ticket.length() > 0) {
                ResultDTO resultDTO = loginService.validTicket(ticket);
                if (resultDTO.isSuccess()) {
                    TicketValidResponse validResponse = (TicketValidResponse) resultDTO.getData();
                    String url = request.getRequestURI();
                    redisTemplate.opsForValue().set(SsoConstant.COOKIE_TOKEN + ":" + validResponse.getToken(), String.valueOf(validResponse.getUserId()), Duration.ofDays(1));
                    response.addCookie(new Cookie(SsoConstant.COOKIE_TOKEN, validResponse.getToken()));
                    response.sendRedirect(url);
                    return false;
                }
            }
        }
        if (valid) {
            return true;
        }
        response.sendRedirect(loginUrl);
        return false;
    }
}

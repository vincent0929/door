package com.vc.door.web.interceptor;

import com.vc.door.core.service.LoginService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static com.vc.door.core.constant.DoorConstants.COOKIE_TOKEN;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginService loginService;

    @Value("${door.login.url}")
    private String loginUrl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (ArrayUtils.isNotEmpty(cookies)) {
            Cookie tokenCookie = Arrays.stream(cookies).filter(cookie -> COOKIE_TOKEN.equals(cookie.getName()))
                    .findFirst().orElse(null);
            if (tokenCookie != null) {
                token = tokenCookie.getValue();
            }
        }

        boolean valid = loginService.validToken(token);
        if (valid) {
            String ticket = loginService.grantTicket(token);
            String callback = request.getParameter( "callback");
            if (callback == null || callback.isEmpty()) {
                return true;
            }
            response.sendRedirect(callback + "?ticket=" + ticket);
        } else {
            response.sendRedirect(loginUrl);
        }
        return false;
    }
}

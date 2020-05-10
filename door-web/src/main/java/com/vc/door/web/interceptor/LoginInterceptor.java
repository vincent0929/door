package com.vc.door.web.interceptor;

import com.vc.door.core.constant.DoorConstants;
import com.vc.door.core.service.LoginService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vc.door.core.constant.DoorConstants.COOKIE_TOKEN;

@Component
@CrossOrigin(origins = "*")
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginService loginService;

    @Value("${door.login.html}")
    private String loginHtml;

    @Value("${door.login.index}")
    private String index;

    private static final String loginPath = "/login";
    private static final String logoutPath = "/logout";

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
            response.addCookie(new Cookie(DoorConstants.TICKET, ticket));
            String callback = request.getParameter(DoorConstants.CALLBACK);
            if (callback == null || callback.isEmpty()) {
                response.sendRedirect(index);
            } else {
                response.sendRedirect(URLDecoder.decode(callback, StandardCharsets.UTF_8.displayName()));
            }
            return false;
        } else {
            String uri = request.getRequestURI();
            if (logoutPath.equals(uri)) {
                return true;
            }
            if (loginPath.equals(uri)) {
                String callback = request.getParameter(DoorConstants.CALLBACK);
                response.sendRedirect(loginHtml + (StringUtils.isBlank(callback) ? "" : "?" + DoorConstants.CALLBACK + "="
                        + URLEncoder.encode(callback, StandardCharsets.UTF_8.displayName())));
                return false;
            }
            String requestUrl = request.getRequestURL().toString();
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!parameterMap.isEmpty()) {
                requestUrl += "?" + parameterMap.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + (entry.getValue() != null ? entry.getValue()[0] : ""))
                        .collect(Collectors.joining("&"));
            }
            response.sendRedirect(loginHtml + "?" + DoorConstants.CALLBACK + "="
                    + URLEncoder.encode(requestUrl, StandardCharsets.UTF_8.displayName()));
        }
        return false;
    }
}

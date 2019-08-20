package com.vc.door.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.vc.door.core.constant.LogActionTypeEnum;
import com.vc.door.core.dao.LoginLogDAO;
import com.vc.door.core.entity.LoginLogDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginLogManager {

    @Autowired
    private LoginLogDAO loginLogDAO;

    @Autowired
    private AppManager appManager;

    public void addLoginLog(Long userId, Long accountId, Long appId, String ip, String token, String appToken) {
        LoginLogDO loginLogDO = buildLoginLog(userId, appId, ip);
        loginLogDO.setActionType(LogActionTypeEnum.LOGIN.getCode());
        JSONObject info = new JSONObject();
        info.put("token", token);
        info.put("appToken", appToken);
        info.put("accountId", accountId);
        loginLogDO.setInfo(info.toJSONString());
        add(loginLogDO);
    }

    public void addLogoutLog(Long userId, Long appId, String ip, String token, String appToken) {
        LoginLogDO loginLogDO = buildLoginLog(userId, appId, ip);
        loginLogDO.setActionType(LogActionTypeEnum.LOGOUT.getCode());
        JSONObject info = new JSONObject();
        info.put("token", token);
        info.put("appToken", appToken);
        loginLogDO.setInfo(info.toJSONString());
        add(loginLogDO);
    }

    public void addValidTicketLog(Long userId, Long appId, String ip, String ticket, String token) {
        LoginLogDO loginLogDO = buildLoginLog(userId, appId, ip);
        JSONObject info = new JSONObject();
        info.put("token", token);
        info.put("ticket", ticket);
        loginLogDO.setInfo(info.toJSONString());
    }

    private LoginLogDO buildLoginLog(Long userId, Long appId, String ip) {
        LoginLogDO loginLogDO = new LoginLogDO();
        loginLogDO.setUserId(userId);
        loginLogDO.setAppId(appId);
        loginLogDO.setIp(ip);
        return loginLogDO;
    }

    private void add(LoginLogDO logDO) {
        loginLogDAO.add(logDO);
    }
}

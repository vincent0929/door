package com.vc.door.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.vc.door.core.constant.LogActionTypeEnum;
import com.vc.door.core.entity.LoginLogDO;
import com.vc.door.core.manager.LoginLogManager;
import com.vc.door.core.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LoginLogManager loginLogManager;

    @Override
    public void addLoginLog(Long userId, Long accountId, Long appId, String ip, String token, String appToken) {
        LoginLogDO loginLogDO = buildLoginLog(userId, appId, ip);
        loginLogDO.setActionType(LogActionTypeEnum.LOGIN.getCode());
        JSONObject info = new JSONObject();
        info.put("token", token);
        info.put("appToken", appToken);
        info.put("accountId", accountId);
        loginLogDO.setInfo(info.toJSONString());
        loginLogManager.addLoginLog(loginLogDO);
    }

    @Override
    public void addLogoutLog(Long userId, Long appId, String ip, String token, String appToken) {
        LoginLogDO loginLogDO = buildLoginLog(userId, appId, ip);
        loginLogDO.setActionType(LogActionTypeEnum.LOGOUT.getCode());
        JSONObject info = new JSONObject();
        info.put("token", token);
        info.put("appToken", appToken);
        loginLogDO.setInfo(info.toJSONString());
        loginLogManager.addLoginLog(loginLogDO);
    }

    @Override
    public void addValidTicketLog(Long userId, Long appId, String ip, String ticket, String token) {
        LoginLogDO loginLogDO = buildLoginLog(userId, appId, ip);
        JSONObject info = new JSONObject();
        info.put("token", token);
        info.put("ticket", ticket);
        loginLogDO.setInfo(info.toJSONString());
        loginLogManager.addLoginLog(loginLogDO);
    }

    private LoginLogDO buildLoginLog(Long userId, Long appId, String ip) {
        LoginLogDO loginLogDO = new LoginLogDO();
        loginLogDO.setUserId(userId);
        loginLogDO.setAppId(appId);
        loginLogDO.setIp(ip);
        return loginLogDO;
    }
}

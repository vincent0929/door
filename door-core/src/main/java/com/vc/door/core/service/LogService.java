package com.vc.door.core.service;

public interface LogService {

    void addLoginLog(Long userId, Long accountId, Long appId, String ip, String token, String appToken);

    void addLogoutLog(Long userId, Long appId, String ip, String token, String appToken);

    void addValidTicketLog(Long userId, Long appId, String ip, String ticket, String token);
}

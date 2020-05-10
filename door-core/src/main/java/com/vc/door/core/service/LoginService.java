package com.vc.door.core.service;

import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.entity.UserDO;

public interface LoginService {

    boolean validToken(String token);

    Long validTicket(String ticket, String appName, String appKey, String appSecret);

    String login(String identifier, String credential, IdentityTypeEnum identityType);

    UserDO getUser(String token);

    String grantTicket(String token);

    void logoutByToken(String token);

    void logoutByAppToken(String appToken);
}

package com.vc.door.core.manager;

import com.vc.door.core.dao.LoginLogDAO;
import com.vc.door.core.entity.LoginLogDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginLogManager {

    @Autowired
    private LoginLogDAO loginLogDAO;

    public void addLoginLog(LoginLogDO loginLogDO) {
        loginLogDAO.add(loginLogDO);
    }
}

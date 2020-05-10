package com.vc.door.core.manager;

import com.vc.door.core.dao.UserDAO;
import com.vc.door.core.entity.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserManager {

    @Autowired
    private UserDAO userDAO;

    public UserDO get(Long id) {
        return userDAO.get(id);
    }
}

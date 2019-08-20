package com.vc.door.core.dao;

import com.vc.door.core.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserDAO {

    Long add(UserDAO userDAO);

    Long delete(@Param("id") Long id);

    UserDO get(@Param("id") Long id);
}

package com.vc.door.core.dao;

import com.vc.door.core.entity.AccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AccountDAO {

    Long add(AccountDO accountDO);

    Long verifySuccess(@Param("id") Long id);

    AccountDO get(@Param("id") Long id);

    AccountDO getByIdentifier(@Param("identifier") String identifier, @Param("credential") String credential,
                              @Param("identityType") Integer identityType);

    List<AccountDO> getByUserId(@Param("id") Long id);

    Long delete(@Param("id") Long id);

    Long deleteByUserId(@Param("userId") Long userId);
}

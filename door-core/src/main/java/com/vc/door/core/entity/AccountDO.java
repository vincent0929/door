package com.vc.door.core.entity;

import io.github.vincent0929.common.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@EqualsAndHashCode(callSuper = true)
@Data
@Alias("account")
public class AccountDO extends BaseDO {

    private Long userId;

    private Integer identityType;

    private String identifier;

    private String credential;

    private Integer verified;
}

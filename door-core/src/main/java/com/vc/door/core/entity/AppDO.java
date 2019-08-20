package com.vc.door.core.entity;

import io.github.vincent0929.common.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@EqualsAndHashCode(callSuper = true)
@Data
@Alias("app")
public class AppDO extends BaseDO {

    private String name;

    private String appKey;

    private String appSecret;

    private String logoutUrl;

    private Integer status;
}

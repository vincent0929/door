package com.vc.door.core.constant;

import io.github.vincent0929.common.constant.BaseEnum;
import lombok.Getter;

@Getter
public enum IdentityTypeEnum implements BaseEnum<Integer> {

    ACCOUNT_PASSWORD(1, "账号密码"), MAIL(2, "邮箱"), PHONE(3, "电话");

    private Integer code;

    private String desc;

    IdentityTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

package com.vc.door.core.constant;

import io.github.vincent0929.common.constant.BaseEnum;
import lombok.Getter;

@Getter
public enum BizErrorEnum implements BaseEnum<Integer> {

    // app相关
    APP_NOT_EXIST(1000, "app没有注册"),
    APP_NOT_ENABLE(1001, "app未启用"),
    APP_KEY_ERROR(1002, "app密钥错误"),

    // ticket相关
    TICKET_ERROR(2001, "ticket不正确"),

    // account相关
    ACCOUNT_ERROR(3001, "账号或密码错误"),

    // token相关
    USER_NOT_LOGIN(4001, "用户未登录")
    ;

    private Integer code;

    private String desc;

    BizErrorEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

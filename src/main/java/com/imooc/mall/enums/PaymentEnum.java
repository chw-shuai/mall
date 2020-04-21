package com.imooc.mall.enums;

import lombok.Getter;

@Getter
public enum PaymentEnum {

    PAY_ONLINE(1),
    ;
    Integer code;

    PaymentEnum(Integer code) {
        this.code = code;
    }
}

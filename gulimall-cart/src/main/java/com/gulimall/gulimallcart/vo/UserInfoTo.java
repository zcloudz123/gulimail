package com.gulimall.gulimallcart.vo;

import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-22-10:08
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;

    private Boolean hasTempUser;
}

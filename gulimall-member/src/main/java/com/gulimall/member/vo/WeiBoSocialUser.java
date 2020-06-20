package com.gulimall.member.vo;

import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-20-15:15
 */
@Data
public class WeiBoSocialUser {
    private String access_token;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private String isRealName;
}

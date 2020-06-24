package com.gulimall.order.vo;

import com.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-13:44
 */
@Data
public class SubmitOrderRespVo {
    private OrderEntity order;
    private Integer code;//0成功
}

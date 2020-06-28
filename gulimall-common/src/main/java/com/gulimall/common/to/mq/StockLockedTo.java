package com.gulimall.common.to.mq;

import lombok.Data;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-9:53
 */
@Data
public class StockLockedTo {
    private Long taskId;

    private StockDetailTo stockDetailTo;
}

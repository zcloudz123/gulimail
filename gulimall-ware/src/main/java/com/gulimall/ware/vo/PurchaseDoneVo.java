package com.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-13-9:57
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;
    private List<PurchaseDoneItemVo> items;
}

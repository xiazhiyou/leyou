package com.leyou.order.dto;

import com.leyou.common.dto.CartDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * dto: orderDataTransferObject
 * 三个字段正好对应前端页面http://api.leyou.com/api/order-service/order 的三个字段
 * carts又是一个集合  包含了商品信息，所以又定义一个cartDTO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @NotNull //数据校验不能为空
    private Long addressId; // 收获人地址id

    private Integer paymentType;// 付款类型

    private List<CartDTO> carts;// 订单详情
}
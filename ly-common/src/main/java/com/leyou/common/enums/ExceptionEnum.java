package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    INVALID_SIGN_ERROR(500,"无效签名"),
    UPDATE_ORDER_STATUS_ERROR(500,"修改订单参数异常"),
    INVALID_ORDER_PARAM(404,"无效的订单参数"),
    ORDER_STATUS_ERROR(404,"订单状态错误"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    ORDER_DETAIL_NOT_FOUNT(404,"订单详情不存在"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    CREATE_ORDER_ERROR(500,"创建订单失败"),
    WX_PAY_ORDER_FAIL(404,"微信下单失败"),
    CART_NOT_FOUND(404,"购物车为空"),
    NO_AUTHORIZED(403,"未授权"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败"),
    INVALID_USERNAME_PASSWORD(404,"用户名或密码错误"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USER_DATA_TYPE(400,"用户数据类型无效"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    CATEGORY_NOT_FOUND(404,"商品分类没查到"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数不存在"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(500,"无效的文件类型"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_UPDATE_ERROR(500,"更新商品失败"),
    GOODS_ID_CANNOT_BE_NULL(500,"更新id不能为空"),
    SKU_NOT_FOUND, STOCK_NOT_FOUND;
    private int code;
    private String msg;
}

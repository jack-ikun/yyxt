package com.kun.order.service;

import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-13-20:55
 */
public interface WeixinService {

    /**
     * 根据订单号下单，生成支付链接
     */
    Map createNative(Long orderId);

    /**
     * 根据订单号去微信第三方查询支付状态
     */
    Map queryPayStatus(Long orderId, String paymentType);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);


}

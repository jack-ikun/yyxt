package com.kun.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.model.order.OrderInfo;
import com.kun.model.order.PaymentInfo;

import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-13-20:41
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    /**
     * 保存交易记录
     * @param order
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    void savePaymentInfo(OrderInfo order, Integer paymentType);

    void paySuccess(String out_trade_no, Integer status, Map<String, String> resultMap);

    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);

}


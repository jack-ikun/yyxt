package com.kun.order.service;

import com.alipay.api.AlipayApiException;
import com.kun.common.result.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-14-21:07
 */
public interface AliPayService {
    Result createNative(Long orderId) throws AlipayApiException;

    void updateOrderStatus(String out_trade_no, HashMap<String,String> map);

}

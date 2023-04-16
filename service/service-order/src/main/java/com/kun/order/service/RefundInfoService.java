package com.kun.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.model.order.PaymentInfo;
import com.kun.model.order.RefundInfo;

/**
 * @author jiakun
 * @create 2023-03-13-22:02
 */
public interface RefundInfoService extends IService<RefundInfo> {
    /**
     * 保存退款记录
     * @param paymentInfo
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);


}

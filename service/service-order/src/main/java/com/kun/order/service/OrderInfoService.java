package com.kun.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.result.Result;
import com.kun.model.order.OrderInfo;
import com.kun.vo.order.OrderCountQueryVo;
import com.kun.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-13-16:18
 */
public interface OrderInfoService extends IService<OrderInfo> {

    //保存订单
    Long saveOrder(String scheduleId, Long patientId);

    /**
     * 分页列表
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    /**
     * 获取订单详情
     */
    OrderInfo getOrder(String orderId);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    Map<String,Object> show(Long orderId);

    /**
     * 取消订单
     * @param orderId
     */
    Result cancelOrder(Long orderId);

    /**
     * 就诊提醒
     */
    void patientTips();

    /**
     * 订单统计
     */
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);




}

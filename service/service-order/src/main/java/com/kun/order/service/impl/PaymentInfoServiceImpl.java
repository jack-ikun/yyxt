package com.kun.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.exception.MyYyxtException;
import com.kun.common.result.ResultCodeEnum;
import com.kun.common.utils.HttpRequestHelper;
import com.kun.common.utils.MD5;
import com.kun.enums.OrderStatusEnum;
import com.kun.enums.PaymentStatusEnum;
import com.kun.hosp.HospitalFeignClient;
import com.kun.model.order.OrderInfo;
import com.kun.model.order.PaymentInfo;
import com.kun.order.mapper.PaymentInfoMapper;
import com.kun.order.service.OrderInfoService;
import com.kun.order.service.PaymentInfoService;
import com.kun.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-13-20:42
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",order.getId());
        wrapper.eq("payment_type",paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0) return;//已存在

        //保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());

        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());

        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);

    }

    /**
     * 支付成功 ：处理支付表
     * @param out_trade_no
     * @param status
     * @param resultMap
     */
    @Override
    public void paySuccess(String out_trade_no, Integer status, Map<String, String> resultMap) {
        PaymentInfo paymentInfo = this.getPaymentInfo(out_trade_no, status);
        if (null == paymentInfo) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        if(paymentInfo.getPaymentStatus().equals(PaymentStatusEnum.UNPAID.getStatus())){
            return;
        }

        PaymentInfo paymentInfoByUp = new PaymentInfo();
        paymentInfoByUp.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfoByUp.setTradeNo(resultMap.get("transaction_id"));
        paymentInfoByUp.setCallbackTime(new Date());
        paymentInfoByUp.setCallbackContent(resultMap.get("resultContent"));
        this.updatePaymentInfo(out_trade_no, paymentInfoByUp);

        //修改订单状态
        OrderInfo orderInfo = orderInfoService.getById(paymentInfo.getOrderId());

        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        // 调用医院接口，通知更新支付状态

        //获取签名
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());

        if( null == signInfoVo){
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("hoscode",orderInfo.getHoscode());
        map.put("hosRecordId",orderInfo.getHosRecordId());
        map.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = MD5.encrypt(signInfoVo.getSignKey());
        map.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(map, signInfoVo.getApiUrl()+"/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new MyYyxtException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

    }

    /**
     * 获取支付记录
     */
    private PaymentInfo getPaymentInfo(String outTradeNo, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 更改支付记录
     */
    private void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoUpd) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        baseMapper.update(paymentInfoUpd, queryWrapper);
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }


}

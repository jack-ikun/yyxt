package com.kun.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.github.wxpay.sdk.WXPayUtil;
import com.kun.common.result.Result;
import com.kun.enums.PaymentTypeEnum;
import com.kun.hosp.HospitalFeignClient;
import com.kun.model.order.OrderInfo;
import com.kun.order.service.AliPayService;
import com.kun.order.service.OrderInfoService;
import com.kun.order.service.PaymentInfoService;
import com.kun.order.utils.AliPayConstantPropertiesUtils;
import com.kun.order.utils.ConstantPropertiesUtils;
import com.kun.order.utils.HttpClient;
import com.kun.vo.hosp.ScheduleOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jiakun
 * @create 2023-03-14-21:07
 */
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public Result createNative(Long orderId) throws AlipayApiException {


        OrderInfo order = orderInfoService.getById(orderId);

        //0 待支付 -1 取消预约 1 已支付
        if(order.getOrderStatus() != 0){
            return Result.ok(false).message("无需支付！");
        }

        //获得初始化的 AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(
                AliPayConstantPropertiesUtils.GATEWAYURL,
                AliPayConstantPropertiesUtils.APPID,
                AliPayConstantPropertiesUtils.MERCHANTPRIVATEKEY
                , "json"
                , AliPayConstantPropertiesUtils.CHARSET
                , AliPayConstantPropertiesUtils.ALIPUBLICKEY
                , AliPayConstantPropertiesUtils.SIGNTYPE);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AliPayConstantPropertiesUtils.RETURNURL);
        alipayRequest.setNotifyUrl(AliPayConstantPropertiesUtils.NOTIFYURL);

        //保存支付记录
        paymentInfoService.savePaymentInfo(order, PaymentTypeEnum.ALIPAY.getStatus());

        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(order.getScheduleId());
        String subject = scheduleOrderVo.getHosname()+"/"+scheduleOrderVo.getDepname();
        String body = "取号时间："+order.getFetchTime()+" 取号地点"+order.getFetchAddress()+"就诊人："+order.getPatientName();
        String outTradeNo = order.getOutTradeNo();
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                + "\"total_amount\":\""+ order.getAmount() +"\","
                + "\"subject\":\""+ subject+"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

/*        String orderJsonStr = (String) redisTemplate.opsForValue().get(orderId.toString());

        if(StringUtils.isEmpty(orderJsonStr)){
            redisTemplate.opsForValue().set(orderId.toString(),JSONObject.toJSONString(order),30, TimeUnit.MINUTES);
        }*/
        String result = alipayClient.pageExecute(alipayRequest).getBody();


        return Result.ok(result);


    }

    @Override
    public void updateOrderStatus(String out_trade_no,HashMap<String,String> map) {

        //支付完成
        paymentInfoService.paySuccess(out_trade_no,PaymentTypeEnum.ALIPAY.getStatus(),map);
    }


}

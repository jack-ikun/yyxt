package com.kun.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.kun.enums.PaymentTypeEnum;
import com.kun.enums.RefundStatusEnum;
import com.kun.model.order.OrderInfo;
import com.kun.model.order.PaymentInfo;
import com.kun.model.order.RefundInfo;
import com.kun.order.service.OrderInfoService;
import com.kun.order.service.PaymentInfoService;
import com.kun.order.service.RefundInfoService;
import com.kun.order.service.WeixinService;
import com.kun.order.utils.ConstantPropertiesUtils;
import com.kun.order.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jiakun
 * @create 2023-03-13-21:04
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderInfoService orderService;
    @Autowired
    private PaymentInfoService paymentService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundInfoService refundInfoService;


    @Override
    public Map createNative(Long orderId) {

        try {
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());

            if( null != payMap) return payMap;
            OrderInfo order = orderService.getById(orderId);
            //保存交易记录
            paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            //
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = order.getReserveDate() + "就诊"+ order.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", order.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            httpClient.setHttps(true);
            httpClient.post();
            //3、返回第三方的数据
            String xml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));

            if(null  != resultMap.get("code_url")){
                redisTemplate.opsForValue().set(orderId.toString(),map,30, TimeUnit.MINUTES);
            }
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    @Override
    public Map queryPayStatus(Long orderId, String paymentType) {
        try {
            OrderInfo orderInfo = orderService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Boolean refund(Long orderId) {
        try {
            PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());

            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfoQuery);
            if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfoQuery.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfoQuery.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfoQuery.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
//3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

}

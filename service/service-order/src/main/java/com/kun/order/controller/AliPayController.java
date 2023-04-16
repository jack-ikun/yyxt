package com.kun.order.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.kun.common.result.Result;
import com.kun.enums.PaymentTypeEnum;
import com.kun.model.order.PaymentInfo;
import com.kun.order.service.AliPayService;
import com.kun.order.service.OrderInfoService;
import com.kun.order.service.PaymentInfoService;
import com.kun.order.service.WeixinService;
import com.kun.order.utils.AliPayConstantPropertiesUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-13-21:25
 */
@Controller
@RequestMapping("/api/order/alipay")
public class AliPayController {
    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private OrderInfoService orderInfoService;
    /**
     * 调用支付宝支付，返回支付界面
     */
    @ResponseBody
    @GetMapping("{orderId}")
    public Result createNative(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) throws AlipayApiException {
        return aliPayService.createNative(orderId);
    }


    @RequestMapping("/return")//支付宝回调接口
    @ResponseBody
    public String returnUrlMethod(HttpServletRequest request, HttpSession session) throws AlipayApiException, UnsupportedEncodingException {
        // 获取支付宝GET过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                AliPayConstantPropertiesUtils.ALIPUBLICKEY,
                AliPayConstantPropertiesUtils.CHARSET,
                AliPayConstantPropertiesUtils.SIGNTYPE); //调用SDK验证签名

        // ——请在这里编写您的程序（以下代码仅作参考）——
        if(signVerified) {
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            // 支付宝交易号
            String payOrderNum = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            // 付款金额
            String orderAmount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            // 保存到数据库
            // 1.从Session域中获取OrderVO对象
           // OrderVO orderVO = (OrderVO) session.getAttribute("orderVO");

            // 2.将支付宝交易号设置到OrderVO对象中
            //orderVO.setPayOrderNum(payOrderNum);
            String value = "trade_no:"+payOrderNum+"out_trade_no:"+out_trade_no+"total_amount:"+orderAmount+"";
            HashMap<String, String> resParams = new HashMap<>();
            resParams.put("resultContent",value);
            resParams.put("transaction_id",payOrderNum);
            //支付成功
            aliPayService.updateOrderStatus(out_trade_no,resParams);

            // 3.发送给MySQL的远程接口
           // ResultEntity<String> resultEntity = mySQLRemoteService.saveOrderRemote(orderVO);
           // logger.info("Order save result="+resultEntity.getResult());

            return "trade_no:"+payOrderNum+"<br/>out_trade_no:"+out_trade_no+"<br/>total_amount:"+orderAmount;
        }else {

            // 页面显示信息：验签失败
            return "验签失败";

        }
    }

    @RequestMapping("/notify")
    public void notifyUrlMethod(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {

        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                AliPayConstantPropertiesUtils.ALIPUBLICKEY,
                AliPayConstantPropertiesUtils.CHARSET,
                AliPayConstantPropertiesUtils.SIGNTYPE); //调用SDK验证签名

        //——请在这里编写您的程序（以下代码仅作参考）——

		/* 实际验证过程建议商户务必添加以下校验：
		1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
		4、验证app_id是否为该商户本身。
		*/
        if(signVerified) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");



        }else {//验证失败
            //调试用，写文本函数记录程序运行情况是否正常
            //String sWord = AlipaySignature.getSignCheckContentV1(params);
            //AlipayConfig.logResult(sWord);

        }

    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    @ResponseBody
    public Result queryPayStatus(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {

        PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.ALIPAY.getStatus());

        //调用查询接口
        if (paymentInfo == null) {//出错
            return Result.fail().message("支付出错");
        }
        if (paymentInfo.getPaymentStatus() == 2) {//如果成功
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }



}

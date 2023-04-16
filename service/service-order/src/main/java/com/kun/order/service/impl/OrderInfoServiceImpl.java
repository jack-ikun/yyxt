package com.kun.order.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.client.PatientFeignClient;
import com.kun.common.constant.MqConst;
import com.kun.common.exception.MyYyxtException;
import com.kun.common.result.Result;
import com.kun.common.result.ResultCodeEnum;
import com.kun.common.service.RabbitService;
import com.kun.common.utils.HttpRequestHelper;
import com.kun.common.utils.MD5;
import com.kun.enums.OrderStatusEnum;
import com.kun.hosp.HospitalFeignClient;
import com.kun.model.order.OrderInfo;
import com.kun.model.user.Patient;
import com.kun.order.mapper.OrderMapper;
import com.kun.order.service.OrderInfoService;
import com.kun.order.service.WeixinService;
import com.kun.vo.hosp.ScheduleOrderVo;
import com.kun.vo.msm.MsmVo;
import com.kun.vo.order.*;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author jiakun
 * @create 2023-03-13-16:18
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;



    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //获取就诊人信息
        Patient patient = patientFeignClient.getPatientById(patientId);
        if(patient == null){
            throw  new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }

        //获取下单排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if(scheduleOrderVo == null){
            throw  new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //开始时间之前 或者 结束时间之后 都不能预约
/*        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new MyYyxtException(ResultCodeEnum.TIME_NO);
        }*/
        //剩余预约数小于0
        if(scheduleOrderVo.getAvailableNumber() <= 0) {
            throw new MyYyxtException(ResultCodeEnum.NUMBER_NO);
        }

        //获取医院签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if(null == signInfoVo) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());

        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());

        //本地入库
        this.save(orderInfo);

        //发送给医院系统的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("scheduleId",scheduleId);
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());

        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());

        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", MD5.encrypt(sign));

        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl()+"/order/submitOrder");

        if(result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //排班可预约数-yyxt-update
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数-yyxt - update
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //发送mq信息更新号源和短信通知
            //封装OrderMQVo 发送实体消息
            OrderMqVo orderMqVo = new OrderMqVo();

            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);

            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            //msmVo.setTemplateCode();

            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);


        } else {
            throw new MyYyxtException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

        return orderInfo.getId();

    }

    //订单列表（条件查询带分页）
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        Long userId = orderQueryVo.getUserId();
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(userId)) {
            wrapper.like("user_id",userId);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        if(!StringUtils.isEmpty(orderQueryVo.getOutTradeNo())) {
            wrapper.like("out_trade_no",orderQueryVo.getOutTradeNo());
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(Integer.parseInt(orderId));
        return this.packOrderInfo(orderInfo);
    }

    @Override
    public Map<String, Object> show(Long orderId) {
        Map<String, Object> map = new HashMap<>();
        OrderInfo orderInfo = this.packOrderInfo(this.getById(orderId));
        map.put("orderInfo", orderInfo);
        Patient patient
                =  patientFeignClient.getPatientById(orderInfo.getPatientId());
        map.put("patient", patient);
        return map;
    }

    @Override
    public Result cancelOrder(Long orderId) {
        OrderInfo orderInfo = this.getById(orderId);
        //当前时间大约退号时间，不能取消预约
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());

        if(quitTime.isBeforeNow()) {
            return Result.ok(false).message(ResultCodeEnum.CANCEL_ORDER_NO.getMessage());
        }

        //获取取消订单的排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(orderInfo.getScheduleId());
        if(scheduleOrderVo == null){
            throw  new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }

        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }

        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        reqMap.put("depcode",orderInfo.getDepcode());
        reqMap.put("amount",orderInfo.getAmount());
        reqMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        reqMap.put("scheduleId", orderInfo.getScheduleId());
        String sign =MD5.encrypt(signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        //是否支付
        if(orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
            //已支付 不能取消预约
            // boolean isRefund = weixinService.refund(orderId);
            // if(!isRefund) {
            //     throw new MyYyxtException(ResultCodeEnum.CANCEL_ORDER_FAIL);
            // }
            return Result.ok(false).message("订单已支付，不能取消预约！");
        }

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updateCancelStatus");

        if(result.getInteger("code") != 200) {
            throw new MyYyxtException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            //更改订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            this.updateById(orderInfo);
            //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        }
        return Result.ok(true);
    }

    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);
        for(OrderInfo orderInfo : orderInfoList) {
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
            }
        }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {

        Map<String, Object> map = new HashMap<>();
        List<OrderCountVo> orderCountVos = baseMapper.selectOrderCount(orderCountQueryVo);
        //日期列表
        List<String> dateList = orderCountVos.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());

        //统计列表
        List<Integer> countList
                =orderCountVos.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        map.put("dateList", dateList); // x
        map.put("countList", countList); // y

        return map;
    }


    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        String depName = hospitalFeignClient.getDepName(orderInfo.getHoscode(), orderInfo.getDepcode());
        orderInfo.setDepname(depName);
        return orderInfo;
    }

}

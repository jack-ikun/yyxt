package com.kun.hosp;

import com.kun.vo.hosp.ScheduleOrderVo;
import com.kun.vo.order.SignInfoVo;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author jiakun
 * @create 2023-03-13-16:47
 */
@Repository
@FeignClient(value = "service-hosp")
public interface HospitalFeignClient {
    /**
     * 根据排班id获取预约下单数据
     */
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);
    /**
     * 获取医院签名信息
     */
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

    @GetMapping("/api/hosp/hospital/inner/getDeptName/{hoscode}/{depcode}")
    String getDepName(
            @PathVariable("hoscode") String hoscode,
            @PathVariable("depcode") String depcode);
}



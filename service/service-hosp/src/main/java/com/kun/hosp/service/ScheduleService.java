package com.kun.hosp.service;

import com.kun.model.hosp.Schedule;
import com.kun.vo.hosp.ScheduleOrderVo;
import com.kun.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-02-18:02
 */
public interface ScheduleService {

    /**
     * 上传排班信息
     * @param paramMap
     */
    void save(Map<String,Object> paramMap);

    /**
     * 分页条件查询
     * @param page
     * @param limit
     * @param scheduleQueryVo
     * @return
     */
    Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo);

    /**
     * 删除排班
     * @param hoscode
     * @param hosScheduleId
     */
    void remove(String hoscode, String hosScheduleId);

    //查询科室所有排班信息。根据医院编号+科室编号
    Map<String,Object> getScheduleInfo(long page,long limit,String hoscode,String depcode);

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    /**
     * 获取排班可预约日期数据
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode);

    /**
     * 根据id获取排班
     * @param id
     * @return
     */
    Schedule getById(String id);

    //根据排班id获取预约下单排班数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 修改排班 可预约数
     */
    void update(Schedule schedule);


}

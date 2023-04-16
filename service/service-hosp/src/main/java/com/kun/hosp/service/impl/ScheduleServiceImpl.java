package com.kun.hosp.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kun.common.exception.MyYyxtException;
import com.kun.common.result.ResultCodeEnum;
import com.kun.common.utils.BeanUtils;
import com.kun.model.hosp.BookingRule;
import com.kun.model.hosp.Department;
import com.kun.model.hosp.Hospital;
import com.kun.model.hosp.Schedule;
import com.kun.hosp.repository.ScheduleRepository;
import com.kun.hosp.service.DepartmentService;
import com.kun.hosp.service.HospitalService;
import com.kun.hosp.service.ScheduleService;
import com.kun.vo.hosp.BookingScheduleRuleVo;
import com.kun.vo.hosp.ScheduleOrderVo;
import com.kun.vo.hosp.ScheduleQueryVo;
import org.ehcache.core.util.CollectionUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班业务操作mongo数据库
 * @author jiakun
 * @create 2023-03-02-18:02
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paramMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);

        Schedule schedule1 = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if(schedule1 !=null){
            //修改
            BeanUtils.copyBean(schedule,schedule1,Schedule.class);
            schedule1.setUpdateTime(new Date());
            schedule1.setIsDeleted(0);
            scheduleRepository.save(schedule1);
        }else {
            //新增
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);

        }
    }

    @Override
    public Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo) {
        //分页
        Pageable pages = PageRequest.of(page - 1, limit);
        
        //查询参数转换
        Schedule schedule = new Schedule();
        org.springframework.beans.BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);

        //创建匹配器 ---忽略大小写+模糊匹配
        ExampleMatcher matching = ExampleMatcher.matching()
                .withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Schedule> e = Example.of(schedule, matching);


        return scheduleRepository.findAll(e, pages);
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //先根据参数查询
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(schedule != null){
            //根据id删除
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    @Override
    public Map<String, Object> getScheduleInfo(long page, long limit, String hoscode, String depcode) {
        //1 根据医院编号 和 科室编号 查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //2.根据工作日进行分组
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        //3.统计号数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.DESC, "workDate"),

                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );

        AggregationResults<BookingScheduleRuleVo> aggResults = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        //分组查询总记录数
        Aggregation aggTotal= Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")

        );
        AggregationResults<BookingScheduleRuleVo> aggTotalResult = mongoTemplate.aggregate(aggTotal, Schedule.class, BookingScheduleRuleVo.class);
        int total = aggTotalResult.getMappedResults().size();

        //获取记录对应的日期 对应周几？
        for (BookingScheduleRuleVo bookingScheduleRuleVo : aggResults.getMappedResults()) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("bookingScheduleRuleList",aggResults.getMappedResults());
        map.put("total",total);

        Hospital hospital = hospitalService.getByHoscode(hoscode);

        Map<String, String> baseMap = new HashMap<>();
        if(null != hospital){
            baseMap.put("hosname",hospital.getHosname());
        }
        map.put("baseMap",baseMap);

        return map;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //根据参数查询mongodb
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        scheduleList.stream().forEach(item->this.packageSchedule(item));
        
        return scheduleList;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode) {

        Map<String, Object> map = new HashMap<>();
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(null == hospital) {
            throw new MyYyxtException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约日期分页数据
        IPage iPage = this.getListDate(page, limit, bookingRule);
        //当前可预约日期列表
        List<Date> dateList = iPage.getRecords();
        //获取可预约日期科室剩余预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode")
                .is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> resluts = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        //获取在安排日期内的预约规则
        List<BookingScheduleRuleVo> scheduleRuleVos = resluts.getMappedResults();

        //将有的安排日期 和 排班合并成Map <= 所有安排日期
        Map<Date, BookingScheduleRuleVo> scheduleRuleVoMap = new HashMap<>();

        if(!CollectionUtils.isEmpty(scheduleRuleVos)){
           scheduleRuleVoMap = scheduleRuleVos.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约排版规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0; i < dateList.size(); i++) {
                Date date = dateList.get(i);//所有安排日期
                BookingScheduleRuleVo bookingScheduleRuleVo = scheduleRuleVoMap.get(date);
                if(bookingScheduleRuleVo ==null){// 说明当天没有排班医生
                    bookingScheduleRuleVo =  new BookingScheduleRuleVo();
                    //就诊医生人数
                    bookingScheduleRuleVo.setDocCount(0);
                    //科室剩余预约数
                    bookingScheduleRuleVo.setAvailableNumber(-1);//-1 无号
                }
                //bookingScheduleRuleVo.setWorkDate(date);
                //设置月日
                bookingScheduleRuleVo.setWorkDateMd(date);
                if( i == dateList.size() - 1 && page == iPage.getPages()){
                    //最后一页，最后一条记录
                    bookingScheduleRuleVo.setStatus(1);//即将放号
                }else {
                    bookingScheduleRuleVo.setStatus(0);//正常放号
                }
                //当天预约过了停号事件，不能预约
                if(i==0 && page == 1){
                    DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                    if(stopTime.isBeforeNow()){
                        //停止预约
                        bookingScheduleRuleVo.setStatus(-1);
                    }
                }
                bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //可预约日期规则数据
        map.put("bookingScheduleList",bookingScheduleRuleVoList);//预约日期规则数据
        map.put("total", iPage.getTotal());//总记录数
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        map.put("baseMap",baseMap);

        return map;
    }

    @Override
    public Schedule getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        return this.packageSchedule(schedule);
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {

        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        Schedule schedule = this.getById(scheduleId);
        if(null == schedule) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new MyYyxtException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }

        BeanUtils.copyProperties(schedule,scheduleOrderVo);

        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepname(departmentService.getDeptName(schedule.getHoscode(),schedule.getDepcode()));
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();

        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //当天预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        return scheduleOrderVo;


    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());

        scheduleRepository.save(schedule);
    }

    //获取可预约日期分页数据
    private IPage getListDate(int page, int limit, BookingRule bookingRule) {

        //当天放号时间yyyy-MM-dd HH:mm
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        int cycle = bookingRule.getCycle();
        //如果当天房放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if(releaseTime.isBeforeNow()) cycle +=1;
        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);//0当前日期 1.2.往后推
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        if(end >dateList.size()) end = dateList.size();
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> datePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page,limit,dateList.size()
        );
        datePage.setRecords(pageDateList);
        return datePage;


    }

    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private Schedule packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",departmentService.getDeptName(schedule.getHoscode(),schedule.getDepcode()));

        //设置周几？
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }
    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    private String getDayOfWeek(DateTime workDate) {
        String dayOfWeek="";
        switch (workDate.getDayOfWeek()){
            case DateTimeConstants.SUNDAY:
                dayOfWeek="周日";
                break;
            case DateTimeConstants.JANUARY:
                dayOfWeek="周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek="周二";
                break;
            case DateTimeConstants.MARCH:
                dayOfWeek="周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek="周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek="周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek="周六";
                break;
            default:
                break;
        }
        return dayOfWeek;
    }
}

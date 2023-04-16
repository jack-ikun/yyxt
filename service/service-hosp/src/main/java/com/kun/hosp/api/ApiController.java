package com.kun.hosp.api;

import com.kun.common.exception.MyYyxtException;
import com.kun.common.result.Result;
import com.kun.common.result.ResultCodeEnum;
import com.kun.common.utils.HttpRequestHelper;
import com.kun.model.hosp.Department;
import com.kun.model.hosp.Schedule;
import com.kun.hosp.service.DepartmentService;
import com.kun.hosp.service.HospitalService;
import com.kun.hosp.service.HospitalSetService;
import com.kun.hosp.service.ScheduleService;
import com.kun.vo.hosp.DepartmentQueryVo;
import com.kun.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-02-14:45
 */
@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String) paramMap.get("logoData");
        if (!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll(" ", "+");
            paramMap.put("logoData", logoData);
        }


        hospitalService.save(paramMap);

        return Result.ok();
    }

    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");

        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }

        return Result.ok(hospitalService.getByHoscode(hoscode));

    }

    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.save(paramMap);

        return Result.ok();
    }

    @ApiOperation(value = "获取科室分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");//可为空
        int page = Integer.parseInt((String) paramMap.get("page"));
        int limit = Integer.parseInt((String) paramMap.get("limit"));
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        departmentQueryVo.setDepcode(depcode);

        Page<Department> departments = departmentService.selectPage(page, limit, departmentQueryVo);

        return Result.ok(departments);

    }

    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        //参数校验
        if (StringUtils.isEmpty(hoscode) || StringUtils.isEmpty(depcode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }

        departmentService.remove(hoscode, depcode);

        return Result.ok();
    }

    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");//可为空
        int page = Integer.parseInt((String) paramMap.get("page"));
        int limit = Integer.parseInt((String) paramMap.get("limit"));
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel = scheduleService.selectPage(page, limit, scheduleQueryVo);

        return Result.ok(pageModel);

    }

    @ApiOperation(value = "删除科室")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        //参数校验
        if (StringUtils.isEmpty(hoscode) || StringUtils.isEmpty(hosScheduleId)) {
            throw new MyYyxtException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new MyYyxtException(ResultCodeEnum.SIGN_ERROR);
        }

        scheduleService.remove(hoscode, hosScheduleId);

        return Result.ok();
    }


}

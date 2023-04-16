package com.kun.hosp.controller;

import com.kun.common.result.Result;
import com.kun.hosp.service.HospitalService;
import com.kun.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author jiakun
 * @create 2023-03-02-20:46
 */
@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @ApiOperation(value = "获取医院分页列表")
    @GetMapping("{page}/{limit}")
    public Result list(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "hospitalQueryVo", value = "查询对象", required = false)
            HospitalQueryVo hospitalQueryVo
    ){
        // System.out.println(hospitalQueryVo.getHosname()+"_____________");
        return Result.ok(hospitalService.selectPage(page,limit,hospitalQueryVo));
    }

    @ApiOperation(value = "更新上线状态")
    @PostMapping("updateStatus/{id}/{status}")
    public Result lock(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable("id") String id,
            @ApiParam(name = "status", value = "状态（0：未上线 1：已上线）", required = true)
            @PathVariable("status") Integer status){
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    @ApiOperation(value = "获取医院详情")
    @GetMapping("show/{id}")
    public Result show(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable String id) {
        return Result.ok(hospitalService.show(id));
    }



}

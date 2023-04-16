package com.kun.user.controller;

import com.kun.enums.UserAuthStatusEnum;
import com.kun.user.service.UserInfoService;
import com.kun.common.result.Result;
import com.kun.common.utils.AuthContextHolder;
import com.kun.common.utils.IpUtil;
import com.kun.model.user.UserInfo;
import com.kun.vo.user.LoginVo;
import com.kun.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-06-22:56
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "用户登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo, HttpServletRequest request) {

        loginVo.setIp(IpUtil.getIpAddr(request));

        Map<String, Object> info = userInfoService.login(loginVo);
        return Result.ok(info);
    }

    //用户认证接口
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //传递两个参数，第一个参数用户id，第二个参数认证数据vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        //封装认证结果

        Map<String, Object> params = new HashMap<>();
        params.put("authStatusString",UserAuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        userInfo.setParam(params);
        return Result.ok(userInfo);
    }



}

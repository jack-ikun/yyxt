package com.kun.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.model.user.UserInfo;
import com.kun.vo.user.LoginVo;
import com.kun.vo.user.UserAuthVo;
import com.kun.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-06-22:51
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> login(LoginVo loginVo);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    /**
     * 用户锁定
     * @param userId
     * @param status 0：锁定 1：正常
     */
    void lock(Long userId, Integer status);

    /**
     * 详情
     * @param userId
     * @return
     */
    Map<String, Object> show(Long userId);

    /**
     * 认证审批
     * @param userId
     * @param authStatus 2：通过 -1：不通过
     */
    void approval(Long userId, Integer authStatus);




}

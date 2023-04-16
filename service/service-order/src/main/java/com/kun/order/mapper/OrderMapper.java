package com.kun.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kun.model.order.OrderInfo;
import com.kun.vo.order.OrderCountQueryVo;
import com.kun.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jiakun
 * @create 2023-03-13-16:18
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderInfo> {

    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);

}

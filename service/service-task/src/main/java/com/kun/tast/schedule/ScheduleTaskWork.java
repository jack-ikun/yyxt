package com.kun.tast.schedule;

import com.kun.common.constant.MqConst;
import com.kun.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author jiakun
 * @create 2023-03-13-23:02
 */
@Component
@EnableScheduling
public class ScheduleTaskWork {
    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天8点执行 提醒就诊
     */
    //@Scheduled(cron = "0 0 1 * * ?") //8点
    @Scheduled(cron = "0/30 * * * * ?") //每30秒
    public void task() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }


}

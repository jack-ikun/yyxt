package com.kun.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jiakun
 * @create 2023-03-13-16:09
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.kun"})
@EnableFeignClients(basePackages = {"com.kun"})
@MapperScan("com.kun.order.mapper")
public class ServiceOrder {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrder.class, args);
    }
}


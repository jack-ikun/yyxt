package com.kun.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jiakun
 * @create 2023-03-13-23:35
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = {"com.kun"})
@ComponentScan(basePackages = {"com.kun"})
public class ServiceStatistics {

    public static void main(String[] args) {
        SpringApplication.run(ServiceStatistics.class,args);
    }
}

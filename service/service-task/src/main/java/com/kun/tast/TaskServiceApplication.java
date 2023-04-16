package com.kun.tast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jiakun
 * @create 2023-03-13-22:54
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.kun"})
public class TaskServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(TaskServiceApplication.class,args);
    }
}

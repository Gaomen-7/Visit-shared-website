package com.travelshare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * <p>No datasource validation happens at startup by design (see ADR 0001):
 * the skeleton only needs to serve HTTP; MySQL connectivity is not a startup precondition.
 */
@SpringBootApplication
@MapperScan("com.travelshare.**.mapper")
public class TravelShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelShareApplication.class, args);
    }
}

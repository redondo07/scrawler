package com.ywb.scrawler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ywb.scrawler.dao")
@EnableScheduling
public class ScrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrawlerApplication.class, args);
    }
}

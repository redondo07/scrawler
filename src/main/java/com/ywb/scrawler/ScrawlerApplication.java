package com.ywb.scrawler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ywb.scrawler.dao")
public class ScrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrawlerApplication.class, args);
    }
}

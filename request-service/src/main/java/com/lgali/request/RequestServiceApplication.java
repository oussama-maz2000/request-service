package com.lgali.request;

import java.time.ZoneOffset;
import java.util.TimeZone;
import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import com.lgali.common.CommonConfig;

@Import({ CommonConfig.class })
@SpringBootApplication
@EnableDiscoveryClient
public class RequestServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(RequestServiceApplication.class, args);
    }

    @PostConstruct
    static void started() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }


}

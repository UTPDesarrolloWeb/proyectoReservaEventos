package com.evently.notificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@org.springframework.scheduling.annotation.EnableScheduling
@EnableAsync
public class NotificacionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionServiceApplication.class, args);
    }
}


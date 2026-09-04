package com.pichincha.cdemsaspopenaidocumentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class OptimusApplication {

    public static void main(String[] args) {
        SpringApplication.run(OptimusApplication.class, args);
    }

}
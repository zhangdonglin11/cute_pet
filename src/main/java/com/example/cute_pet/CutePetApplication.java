package com.example.cute_pet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @MapperScan("com.example.cute_pet.mapper")
public class CutePetApplication {

    public static void main(String[] args) {
        SpringApplication.run(CutePetApplication.class, args);
    }

}

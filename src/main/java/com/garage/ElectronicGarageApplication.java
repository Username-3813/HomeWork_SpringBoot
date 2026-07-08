package com.garage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ElectronicGarageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectronicGarageApplication.class, args);
       //System.out.println(new BCryptPasswordEncoder().encode("admin123"));
    }
}
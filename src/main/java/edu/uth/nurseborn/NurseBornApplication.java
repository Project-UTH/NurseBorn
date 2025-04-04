package edu.uth.nurseborn;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NurseBornApplication {

    public static void main(String[] args) {
        SpringApplication.run(NurseBornApplication.class, args);
    }
    @Bean
    public ModelMapper modelMapper() { //Mapping tự động cho models và dtos
        return new ModelMapper();
    }
}


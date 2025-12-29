package com.github.juliusd.ueberboeseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJdbcRepositories
public class UeberboeseApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(UeberboeseApiApplication.class, args);
  }
}

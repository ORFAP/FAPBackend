package de.orfap.fap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FapBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(FapBackendApplication.class, args);
  }
}

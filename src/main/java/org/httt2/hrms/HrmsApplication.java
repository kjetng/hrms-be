package org.httt2.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@PropertySource("file:${user.dir}/.env")
@EnableAsync
public class HrmsApplication {

  public static void main(String[] args) {
    SpringApplication.run(HrmsApplication.class, args);
  }

}

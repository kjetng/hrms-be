package org.httt2.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@PropertySource("file:${user.dir}/.env")
@EnableAsync
@EnableScheduling
public class HrmsApplication {

  public static void main(String[] args) {

    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    System.out.println("✅ JVM TimeZone forced to: " + TimeZone.getDefault().getID());

    SpringApplication.run(HrmsApplication.class, args);
  }

}

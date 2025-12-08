package org.httt2.hrms;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class HrmsApplication {

  public static void main(String[] args) {

    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    System.out.println("✅ JVM TimeZone forced to: " + TimeZone.getDefault().getID());

    SpringApplication.run(HrmsApplication.class, args);
  }

}

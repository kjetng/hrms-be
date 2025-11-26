package org.httt2.hrms.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

  static {
    // Load .env file từ project root và set các biến vào System properties
    Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing() // Nếu không tìm thấy .env file, không ném exception
        .load();

    // Set các environment variables từ .env vào System properties
    // để Spring Boot có thể sử dụng ${DB_URL}, ${DB_USERNAME}, ${DB_PASSWORD}
    if (dotenv != null) {
      dotenv.entries().forEach(entry -> {
        System.setProperty(entry.getKey(), entry.getValue());
      });
    }
  }
}

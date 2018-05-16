package ru.dwfe.net.authtion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class AuthtionApplication
{
  @PostConstruct
  void started()
  {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  public static void main(String[] args)
  {
    SpringApplication.run(AuthtionApplication.class, args);
  }
}

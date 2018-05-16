package ru.dwfe.net.authtion.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthtionCustomConfig
{
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder)
  {
    return restTemplateBuilder
            .setConnectTimeout(5000)
            .setReadTimeout(5000)
            .build();
  }
}

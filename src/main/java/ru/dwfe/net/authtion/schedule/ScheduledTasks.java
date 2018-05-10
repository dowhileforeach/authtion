package ru.dwfe.net.authtion.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.dao.repository.MailingWelcomeWhenPasswordWasNotPassedRepository;

@Component
public class ScheduledTasks
{
  private final static Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

  @Autowired
  public JavaMailSender emailSender;

  @Autowired
  MailingWelcomeWhenPasswordWasNotPassedRepository mailingWelcomeWhenPasswordWasNotPassedRepository;

  @Scheduled(fixedDelay = 30_000)
  public void mailingHighPriority()
  {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo("pistoletik@gmail.com");
//        message.setFrom("noreply@dwfe.ru");
//        message.setSubject("Welcome");
//        message.setText("Welcome to\nDWFE.ru");
//        emailSender.send(message);
//        log.info("sended");

//        mailingWelcomeWhenPasswordWasNotPassedRepository.findAll().forEach(next -> {
//
//        });
  }

  @Scheduled(fixedDelay = 35_000)
  public void mailingStandardPriority()
  {

  }
}

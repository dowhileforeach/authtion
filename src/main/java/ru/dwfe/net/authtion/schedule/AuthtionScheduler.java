package ru.dwfe.net.authtion.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
@PropertySource("classpath:application.properties")
public class AuthtionScheduler
{
  private final static Logger log = LoggerFactory.getLogger(AuthtionScheduler.class);

  private final JavaMailSender emailSender;

  final AuthtionMailingRepository mailingRepository;

  private final Environment env;

  private static final ConcurrentSkipListSet<AuthtionMailing> MAILING_POOL = new ConcurrentSkipListSet<>();

  @Autowired
  public AuthtionScheduler(JavaMailSender emailSender, AuthtionMailingRepository mailingRepository, Environment env)
  {
    this.emailSender = emailSender;
    this.mailingRepository = mailingRepository;
    this.env = env;
  }

  @Scheduled(
          initialDelayString = "${dwfe.authtion.scheduled.task.mailing.initial-delay}",
          fixedRateString = "${dwfe.authtion.scheduled.task.mailing.collect-from-db-interval}")
  public void collectMailingTasksFromDatabase()
  {

    log.warn("before collect mailing tasks from Database");
    MAILING_POOL.addAll(mailingRepository.getNewJob());
    log.warn("collected = {}", MAILING_POOL.size());

//    SimpleMailMessage message = new SimpleMailMessage();
//    message.setTo("pistoletik@gmail.com");
//    message.setFrom("noreply@dwfe.ru");
//    message.setSubject("Welcome");
//    message.setText("Welcome to\nDWFE.ru");
//    emailSender.send(message);
//    log.info("sent");

  }

  @Scheduled(
          initialDelayString = "${dwfe.authtion.scheduled.task.mailing.initial-delay}",
          fixedDelayString = "${dwfe.authtion.scheduled.task.mailing.send-interval}")
  public void mailingWelcomeWhenPasswordWasNotPassed()
  {
    log.warn("mailing before perform");
    List<AuthtionMailing> toDataBase = new ArrayList<>();
    MAILING_POOL.forEach(next -> {
      try
      {
        // next.getMessageText();
        // ВНИМАНИЕ!!! Сначала изменить почтовые ящики на dwfe домен для тестового окружения
        // отправить письмо
        if (next.getType() != 3 && next.getType() != 5)
          next.clear();
        next.setSent(true);
        toDataBase.add(next);
        log.warn("sent");
      }
      catch (Throwable e)
      {
        if (next.getAttempt().incrementAndGet() >= 3)
        {
          next.clear();
          next.setMaxAttemptsReached(true);
          next.setCauseOfLastFailure(e.toString());
          toDataBase.add(next);
        }
        log.warn("to next attempt, after error");
      }
    });

    log.warn("mailing before toDataBase.size() > 0");
    if (toDataBase.size() > 0)
    {
      log.warn("store to DB = {}", toDataBase.size());
      mailingRepository.saveAll(toDataBase);
      MAILING_POOL.removeAll(toDataBase);
    }
  }
}

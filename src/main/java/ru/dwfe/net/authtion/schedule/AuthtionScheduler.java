package ru.dwfe.net.authtion.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
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
  private final int maxAttemptsMailing;
  private final String sendFrom;

  @Autowired
  public AuthtionScheduler(JavaMailSender emailSender, AuthtionMailingRepository mailingRepository, Environment env)
  {
    this.emailSender = emailSender;
    this.mailingRepository = mailingRepository;
    this.env = env;

    String maxAttemptsMailingStr = env.getProperty("dwfe.authtion.scheduled-task-mailing.max-attempts-to-send-if-error");
    this.maxAttemptsMailing = maxAttemptsMailingStr == null ? 3 : Integer.parseInt(maxAttemptsMailingStr);

    this.sendFrom = env.getProperty("spring.mail.username");
  }

  @Scheduled(
          initialDelayString = "${dwfe.authtion.scheduled-task-mailing.initial-delay}",
          fixedRateString = "${dwfe.authtion.scheduled-task-mailing.collect-from-db-interval}")
  public void collectMailingTasksFromDatabase()
  {
    MAILING_POOL.addAll(mailingRepository.getNewJob());
    log.debug("mailing - collected[{}]", MAILING_POOL.size());
  }

  @Scheduled(
          initialDelayString = "#{authtionConfigProperties.scheduledTaskMailing.initialDelay}",
          fixedDelayString = "#{authtionConfigProperties.scheduledTaskMailing.sendInterval}")
  public void sendingMail()
  {
    log.debug("mailing - attempt to send[{}]...", MAILING_POOL.size());
    List<AuthtionMailing> toDataBase = new ArrayList<>();
    MAILING_POOL.forEach(next -> {
      int type = next.getType();
      String email = next.getEmail();
      try
      {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(getMailSubject(type));
        message.setFrom(sendFrom);
        message.setTo(email);
        message.setText(getMailMessageText(type, next.getData()));
        emailSender.send(message);

        next.setSent(true);
        if (type != 3 && type != 5)
          next.clear();

        toDataBase.add(next);
        log.debug("mailing - sent to {}", email);
      }
      catch (Throwable e)
      {
        if (next.getAttempt().incrementAndGet() >= maxAttemptsMailing)
        {
          next.clear();
          next.setMaxAttemptsReached(true);
          next.setCauseOfLastFailure(e.toString());

          toDataBase.add(next);
          log.debug("mailing - last fail sending to {}", email);
        }
        else
          log.debug("mailing - go to attempt({}) after fail, {}", next.getAttempt().get(), email);
      }
    });

    if (toDataBase.size() > 0)
    {
      mailingRepository.saveAll(toDataBase);
      MAILING_POOL.removeAll(toDataBase);
      log.debug("mailing - correct store to DB = {}", toDataBase.size());
    }
  }

  private String getMailSubject(int type)
  {
    String result = "";

    if (type == 1)
      return "Welcome, when password was passed";
    else if (type == 2)
      return "Welcome, when password was not passed";
    else if (type == 3)
      return "Confirm email";
    else if (type == 4)
      return "Password was changed";
    else if (type == 5)
      return "Confirm restore password";

    return result;
  }


  private String getMailMessageText(int type, String data)
  {
    String result = "";

    if (type == 1)
      return "Welcome.";
    else if (type == 2)
      return "Your password: " + data;
    else if (type == 3)
      return "http://localhost:8080/v1/confirm-consumer-email?key=" + data;
    else if (type == 4)
      return "Password was changed";
    else if (type == 5)
      return "http://localhost:8080/v1/confirm-restore-consumer-pass?key=" + data;

    return result;
  }
}


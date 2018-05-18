package ru.dwfe.net.authtion.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;
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

  private final JavaMailSender mailSender;
  private final AuthtionMailingRepository mailingRepository;

  private static final ConcurrentSkipListSet<AuthtionMailing> MAILING_POOL = new ConcurrentSkipListSet<>();
  private final int maxAttemptsMailingIfError;
  private final String sendFrom;
  private final TemplateEngine templateEngine; // Thymeleaf

  @Autowired
  public AuthtionScheduler(JavaMailSender mailSender, AuthtionMailingRepository mailingRepository, Environment env, AuthtionConfigProperties authtionConfigProperties, TemplateEngine templateEngine)
  {
    this.mailSender = mailSender;
    this.mailingRepository = mailingRepository;

    this.maxAttemptsMailingIfError = authtionConfigProperties.getScheduledTaskMailing().getMaxAttemptsToSendIfError();
    this.sendFrom = env.getProperty("spring.mail.username");
    this.templateEngine = templateEngine;
  }

  @Scheduled(
          initialDelayString = "#{authtionConfigProperties.scheduledTaskMailing.initialDelay}",
          fixedRateString = "#{authtionConfigProperties.scheduledTaskMailing.collectFromDbInterval}")
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
      String data = next.getData();
      try
      {
        MimeMessagePreparator preparator = mimeMessage -> {
          MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED);
          helper.setFrom(sendFrom);
          helper.setTo(email);
          helper.setSubject(getMailSubject(type));
          helper.setText(getMailMessageText(type, data), true);
        };
        mailSender.send(preparator);

        next.setSent(true);
        if (type != 3 && type != 5)
          next.clear();

        toDataBase.add(next);
        log.debug("mailing - sent to {}", email);
      }
      catch (Throwable e)
      {
        if (next.getAttempt().incrementAndGet() >= maxAttemptsMailingIfError)
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
    Context context = new Context();

    if (type == 1)
      return "";
    else if (type == 2)
      context.setVariable("data", data);
    else if (type == 3)
      context.setVariable("data", "http://localhost:8080/v1/confirm-consumer-email?key=" + data);
    else if (type == 4)
      return "";
    else if (type == 5)
      context.setVariable("data", "http://localhost:8080/v1/confirm-restore-consumer-pass?key=" + data);

    return templateEngine.process("mailing" + type, context);
  }
}


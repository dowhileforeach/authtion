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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
@PropertySource("classpath:application.properties")
public class AuthtionScheduler
{
  private final static Logger log = LoggerFactory.getLogger(AuthtionScheduler.class);

  private final JavaMailSender mailSender;
  private final AuthtionMailingRepository mailingRepository;
  private final AuthtionConfigProperties authtionConfigProperties;

  private static final ConcurrentSkipListSet<AuthtionMailing> MAILING_POOL = new ConcurrentSkipListSet<>();
  private final int maxAttemptsMailingIfError;
  private final String sendFrom;
  private final TemplateEngine templateEngine; // Thymeleaf

  @Autowired
  public AuthtionScheduler(JavaMailSender mailSender, AuthtionMailingRepository mailingRepository, Environment env, AuthtionConfigProperties authtionConfigProperties, TemplateEngine templateEngine)
  {
    this.mailSender = mailSender;
    this.mailingRepository = mailingRepository;
    this.authtionConfigProperties = authtionConfigProperties;

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
    log.debug("mailing - [{}] collected from DB", MAILING_POOL.size());
  }

  @Scheduled(
          initialDelayString = "#{authtionConfigProperties.scheduledTaskMailing.initialDelay}",
          fixedDelayString = "#{authtionConfigProperties.scheduledTaskMailing.sendInterval}")
  public void sendingMail()
  {
    log.debug("mailing - [{}] before sending", MAILING_POOL.size());
    final var toDataBase = new ArrayList<AuthtionMailing>();
    MAILING_POOL.forEach(next -> {
      var type = next.getType();
      var email = next.getEmail();
      var data = next.getData();
      var subjectMessage = getSubjectMessage(type, data);
      try
      {
        MimeMessagePreparator preparator = mimeMessage -> {
          var helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO);
          helper.setFrom(sendFrom);
          helper.setTo(email);
          helper.setSubject(subjectMessage.get("subject"));
          helper.setText(subjectMessage.get("message"), true);
          //mimeMessage.addHeader("Content-Transfer-Encoding", "base64"); // to auto encode message to base64
          mimeMessage.addHeader("Content-Transfer-Encoding", "quoted-printable");
        };
        mailSender.send(preparator);

        next.setSent(true);
        if (type != 3 && type != 5) // if not confirmation
          next.clear();

        toDataBase.add(next);
        log.debug("mailing - {}, successfully sent", email);
      }
      catch (Throwable e)
      {
        if (next.getAttempt().incrementAndGet() > maxAttemptsMailingIfError)
        {
          if (type != 3 && type != 5) // if not confirmation
            next.clear();             // but all of a sudden the letter was sent

          next.setMaxAttemptsReached(true);
          next.setCauseOfLastFailure(e.toString());

          toDataBase.add(next);
          log.debug("mailing - {}, last fail sending", email);
        }
        else
          log.debug("mailing - {}, go to attempt[{}] after fail", email, next.getAttempt().get());
      }
    });

    if (toDataBase.size() > 0)
    {
      mailingRepository.saveAll(toDataBase);
      MAILING_POOL.removeAll(toDataBase);
      log.debug("mailing - [{}] store to DB", toDataBase.size());
      toDataBase.clear();
    }
  }

  private Map<String, String> getSubjectMessage(int type, String data)
  {
    var result = new HashMap<String, String>();
    var subjKey = "subject";
    var messageKey = "message";
    var dataKey = "data";
    var context = new Context();
    var frontendHost = authtionConfigProperties.getFrontend().getHost();
    var resourceConfirmEmail = authtionConfigProperties.getFrontend().getResourceConfirmEmail();
    var resourceConfirmRestorePass = authtionConfigProperties.getFrontend().getResourceConfirmRestorePass();

    if (type == 1)
    {
      result.put(subjKey, "Welcome, when password was passed");
    }
    else if (type == 2)
    {
      result.put(subjKey, "Welcome, when password was not passed");
      context.setVariable(dataKey, data);
    }
    else if (type == 3)
    {

      result.put(subjKey, "Confirm email");
      context.setVariable(dataKey, frontendHost + resourceConfirmEmail + "?key=" + data);
    }
    else if (type == 4)
    {
      result.put(subjKey, "Password was changed");
    }
    else if (type == 5)
    {
      result.put(subjKey, "Confirm restore password");
      context.setVariable(dataKey, frontendHost + resourceConfirmRestorePass + "?key=" + data);
    }
    result.put(messageKey, templateEngine.process("mailing" + type, context));
    return result;
  }
}


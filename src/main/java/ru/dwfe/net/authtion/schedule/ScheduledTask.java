package ru.dwfe.net.authtion.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.dao.Mailing;
import ru.dwfe.net.authtion.dao.repository.MailingRepository;

import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class ScheduledTask
{
  private final static Logger log = LoggerFactory.getLogger(ScheduledTask.class);

  @Autowired
  public JavaMailSender emailSender;

  @Autowired
  MailingRepository mailingRepository;

  private static final ConcurrentSkipListSet<Mailing> MAILING_POOL = new ConcurrentSkipListSet<>();

  @Scheduled(fixedRate = 60_000, initialDelay = 60_000)
  public void collectMailingTasksFromDatabase()
  {

//    log.warn("before collect mailing tasks from Database");
//    MAILING_POOL.addAll(mailingRepository.getNewJob());
//    log.warn("collected = {}", MAILING_POOL.size());

//    SimpleMailMessage message = new SimpleMailMessage();
//    message.setTo("pistoletik@gmail.com");
//    message.setFrom("noreply@dwfe.ru");
//    message.setSubject("Welcome");
//    message.setText("Welcome to\nDWFE.ru");
//    emailSender.send(message);
//    log.info("sended");

  }

  @Scheduled(fixedDelay = 45_000, initialDelay = 60_000)
  public void mailingWelcomeWhenPasswordWasNotPassed()
  {
//    log.warn("mailing before perform");
//    List<Mailing> toDataBase = new ArrayList<>();
//    MAILING_POOL.forEach(next -> {
//      try
//      {
//        // next.getMessageText();
//        // отправить письмо
//        next.clear();
//        next.setSended(true);
//        toDataBase.add(next);
//        log.warn("sended");
//      }
//      catch (Throwable e)
//      {
//        if (next.getAttempt().incrementAndGet() >= 3)
//        {
//          next.clear();
//          next.setMaxAttemptsReached(true);
//          toDataBase.add(next);
//        }
//        log.warn("to next attempt, after error");
//      }
//    });
//
//    log.warn("mailing before toDataBase.size() > 0");
//    if (toDataBase.size() > 0)
//    {
//      log.warn("store to DB = {}", toDataBase.size());
//      mailingRepository.saveAll(toDataBase);
//      MAILING_POOL.removeAll(toDataBase);
//    }
  }
}

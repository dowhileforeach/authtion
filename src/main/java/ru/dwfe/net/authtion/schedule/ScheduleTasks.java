package ru.dwfe.net.authtion.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.dao.MailingWelcomeWhenPasswordWasNotPassed;
import ru.dwfe.net.authtion.dao.repository.MailingWelcomeWhenPasswordWasNotPassedRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class ScheduleTasks
{
  private final static Logger log = LoggerFactory.getLogger(ScheduleTasks.class);

  @Autowired
  public JavaMailSender emailSender;

  @Autowired
  MailingWelcomeWhenPasswordWasNotPassedRepository mailingWelcomeWhenPasswordWasNotPassedRepository;

  private static final ConcurrentSkipListSet<MailingWelcomeWhenPasswordWasNotPassed> poolOfMailingWelcomeWhenPasswordWasNotPassed = new ConcurrentSkipListSet<>();
  private static final ConcurrentSkipListSet<MailingWelcomeWhenPasswordWasNotPassed> mailingWelcomeWhenPasswordWasNotPassedToDB = new ConcurrentSkipListSet<>();

  @Scheduled(fixedDelay = 60_000)
  public void collectMailingTasksFromDatabase()
  {
    poolOfMailingWelcomeWhenPasswordWasNotPassed.addAll(mailingWelcomeWhenPasswordWasNotPassedRepository.searchByNotSended());

//    SimpleMailMessage message = new SimpleMailMessage();
//    message.setTo("pistoletik@gmail.com");
//    message.setFrom("noreply@dwfe.ru");
//    message.setSubject("Welcome");
//    message.setText("Welcome to\nDWFE.ru");
//    emailSender.send(message);
//    log.info("sended");

  }

  @Scheduled(fixedDelay = 45_000)
  public void mailingWelcomeWhenPasswordWasNotPassed()
  {
    List<MailingWelcomeWhenPasswordWasNotPassed> toDB = new ArrayList<>();
    poolOfMailingWelcomeWhenPasswordWasNotPassed.forEach(next -> {

      // ЦИКЛ начало
      //   ЕСЛИ maxAttemptsReached = false И sended = false
      //      сформировать текст
      //      ПОПЫТКА
      //         отправить письмо
      //         удалить пароль
      //         пометить, sended = true
      //      ИСКЛЮЧЕНИЕ
      //         инкрементировать попытку
      //         ЕСЛИ количество попыток >= max попыток
      //            пометить, maxAttemptsReached = true
      //         КонецЕсли
      //      КонецПопытки
      //   ИНАЧЕ
      //      удалить next из пула отправки
      //      добавить next в список для обновления
      //   КонецЕсли
      // КонецЦикла
    });

    if (toDB.size() > 0)
      mailingWelcomeWhenPasswordWasNotPassedRepository.saveAll(toDB);
  }
}

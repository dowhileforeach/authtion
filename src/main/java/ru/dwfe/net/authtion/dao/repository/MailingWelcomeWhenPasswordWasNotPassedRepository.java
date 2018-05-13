package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingWelcomeWhenPasswordWasNotPassed;

import java.util.List;

@Repository
public interface MailingWelcomeWhenPasswordWasNotPassedRepository extends CrudRepository<MailingWelcomeWhenPasswordWasNotPassed, String>
{
  @Query("FROM MailingWelcomeWhenPasswordWasNotPassed WHERE sended = false AND maxAttemptsReached = false")
  List<MailingWelcomeWhenPasswordWasNotPassed> searchByNotSended();
}

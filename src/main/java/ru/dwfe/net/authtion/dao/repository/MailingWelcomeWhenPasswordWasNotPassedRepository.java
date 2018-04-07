package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingWelcomeWhenPasswordWasNotPassed;

@Repository
public interface MailingWelcomeWhenPasswordWasNotPassedRepository extends CrudRepository<MailingWelcomeWhenPasswordWasNotPassed, String>
{
}

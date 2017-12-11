package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;
import ru.dwfe.net.authtion.dao.User;

public interface MailingNewUserPasswordRepository extends CrudRepository<MailingNewUserPassword, String>
{
}

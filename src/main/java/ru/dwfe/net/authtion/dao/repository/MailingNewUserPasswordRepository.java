package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;

@Repository
public interface MailingNewUserPasswordRepository extends CrudRepository<MailingNewUserPassword, String>
{
}

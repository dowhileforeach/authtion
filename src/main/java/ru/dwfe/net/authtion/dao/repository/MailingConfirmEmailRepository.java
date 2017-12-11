package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;

public interface MailingConfirmEmailRepository  extends CrudRepository<MailingConfirmEmail, String>
{
}

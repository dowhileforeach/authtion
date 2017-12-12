package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;

@Repository
public interface MailingRestorePasswordRepository extends CrudRepository<MailingRestorePassword, String>
{
    MailingRestorePassword findByConfirmKey(String confirmKey);
}

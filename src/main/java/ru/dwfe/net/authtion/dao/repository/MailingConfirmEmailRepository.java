package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;

@Repository
public interface MailingConfirmEmailRepository extends CrudRepository<MailingConfirmEmail, String>
{
    MailingConfirmEmail findByConfirmKey(String confirmKey);
}

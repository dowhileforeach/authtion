package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;

import java.util.Optional;

@Repository
public interface MailingConfirmEmailRepository extends CrudRepository<MailingConfirmEmail, String>
{
    Optional<MailingConfirmEmail> findByConfirmKey(String confirmKey);
}

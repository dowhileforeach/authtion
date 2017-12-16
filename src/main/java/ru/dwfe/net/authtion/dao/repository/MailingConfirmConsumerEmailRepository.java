package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingConfirmConsumerEmail;

import java.util.Optional;

@Repository
public interface MailingConfirmConsumerEmailRepository extends CrudRepository<MailingConfirmConsumerEmail, String>
{
    Optional<MailingConfirmConsumerEmail> findByConfirmKey(String confirmKey);
}

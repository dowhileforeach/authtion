package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingRestoreConsumerPassword;

import java.util.Optional;

@Repository
public interface MailingRestoreConsumerPasswordRepository extends CrudRepository<MailingRestoreConsumerPassword, String>
{
    Optional<MailingRestoreConsumerPassword> findByConfirmKey(String confirmKey);
}

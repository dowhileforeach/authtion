package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;

import java.util.Optional;

@Repository
public interface MailingRestorePasswordRepository extends CrudRepository<MailingRestorePassword, String>
{
  Optional<MailingRestorePassword> findByConfirmKey(String confirmKey);
}

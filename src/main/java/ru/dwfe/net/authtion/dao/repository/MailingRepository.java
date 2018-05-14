package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.Mailing;

import java.util.List;
import java.util.Optional;

@Repository
public interface MailingRepository extends CrudRepository<Mailing, Mailing.MailingId>
{
  @Query("FROM Mailing WHERE sended = false AND maxAttemptsReached = false")
  List<Mailing> getNewJob();

  List<Mailing> findByEmail(String email);

  Optional<Mailing> findByData(String data);
}

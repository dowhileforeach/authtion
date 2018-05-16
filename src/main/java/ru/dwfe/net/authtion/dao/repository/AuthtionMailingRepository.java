package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.AuthtionMailing;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthtionMailingRepository extends CrudRepository<AuthtionMailing, AuthtionMailing.AuthtionMailingId>
{
  @Query("FROM AuthtionMailing WHERE sended = false AND maxAttemptsReached = false")
  List<AuthtionMailing> getNewJob();

  List<AuthtionMailing> findByEmail(String email);

  Optional<AuthtionMailing> findByData(String data);
}

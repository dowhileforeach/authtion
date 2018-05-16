package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.AuthtionMailing;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthtionMailingRepository extends CrudRepository<AuthtionMailing, AuthtionMailing.AuthtionMailingId>
{
  @Query("FROM AuthtionMailing WHERE sent = false AND maxAttemptsReached = false")
  List<AuthtionMailing> getNewJob();

  List<AuthtionMailing> findByEmail(String email);

  Optional<AuthtionMailing> findByData(String data);

  List<AuthtionMailing> findByTypeAndEmail(int type, String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM authtion_mailing WHERE type=:type AND email=:email AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<AuthtionMailing> findLastNotEmptyData(@Param("type") int type, @Param("email") String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM authtion_mailing WHERE type=:type AND email=:email AND sent=true AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<AuthtionMailing> findSentLastNotEmptyData(@Param("type") int type, @Param("email") String email);
}

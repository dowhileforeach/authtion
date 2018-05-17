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

  @Query(nativeQuery = true,
          value = "SELECT * FROM authtion_mailing WHERE type=:type AND email=:email AND data=:data")
  Optional<AuthtionMailing> findData(@Param("type") int type, @Param("email") String email, @Param("data") String data);

  List<AuthtionMailing> findByTypeAndEmail(int type, String email);

  Optional<AuthtionMailing> findByTypeAndData(int type, String data);

  @Query(nativeQuery = true,
          value = "SELECT * FROM authtion_mailing WHERE type=:type AND email=:email AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<AuthtionMailing> findLastNotEmptyData(@Param("type") int type, @Param("email") String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM authtion_mailing WHERE type=:type AND email=:email AND sent=true AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<AuthtionMailing> findLastSentNotEmptyData(@Param("type") int type, @Param("email") String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM authtion_mailing WHERE type=:type AND email=:email AND sent=true AND data<>''")
  List<AuthtionMailing> findSentNotEmptyData(@Param("type") int type, @Param("email") String email);

}

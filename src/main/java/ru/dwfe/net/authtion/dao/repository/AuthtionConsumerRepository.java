package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;

import java.util.Optional;

@Repository
public interface AuthtionConsumerRepository extends JpaRepository<AuthtionConsumer, Long>
{
  Optional<AuthtionConsumer> findByEmail(String email);
}
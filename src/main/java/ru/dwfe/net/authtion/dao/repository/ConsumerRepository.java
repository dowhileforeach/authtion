package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.Consumer;

import java.util.Optional;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long>
{
    Optional<Consumer> findByEmail(String email);
}
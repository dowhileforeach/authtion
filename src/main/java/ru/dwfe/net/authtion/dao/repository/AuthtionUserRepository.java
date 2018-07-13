package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dwfe.net.authtion.dao.AuthtionUser;

public interface AuthtionUserRepository extends JpaRepository<AuthtionUser, Long>
{
}

package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dwfe.net.authtion.dao.AuthtionCountry;

public interface AuthtionCountryRepository extends JpaRepository<AuthtionCountry, String>
{
}

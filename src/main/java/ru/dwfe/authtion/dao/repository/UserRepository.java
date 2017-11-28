package ru.dwfe.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import ru.dwfe.authtion.dao.User;

public interface UserRepository extends CrudRepository<User, String> { }
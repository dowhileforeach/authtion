package ru.dwfe.net.authtion.dao.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.dwfe.net.authtion.dao.ConfirmationKey;

@Repository
public interface ConfirmationKeyRepository extends CrudRepository<ConfirmationKey, String>
{
    ConfirmationKey findByKey(String key);
}

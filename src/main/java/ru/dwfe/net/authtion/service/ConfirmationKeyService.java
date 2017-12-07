package ru.dwfe.net.authtion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dwfe.net.authtion.dao.ConfirmationKey;
import ru.dwfe.net.authtion.dao.repository.ConfirmationKeyRepository;

@Service
public class ConfirmationKeyService
{
    @Autowired
    private ConfirmationKeyRepository repository;

    public ConfirmationKey findByKey(String key)
    {
        return repository.findByKey(key);
    }

    public ConfirmationKey save(ConfirmationKey confirmationKey)
    {
        return repository.save(confirmationKey);
    }

    public void delete(ConfirmationKey confirmationKey)
    {
        repository.delete(confirmationKey);
    }
}
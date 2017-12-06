package ru.dwfe.net.authtion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.dao.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class UserService implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException
    {
        return repository.findById(id).orElseThrow(() -> {
            String str = String.format("The user doesn't exist: %s", id);
            log.error(str);
            return new UsernameNotFoundException(str);
        });
    }

    public Optional<User> findById(String id)
    {
        return repository.findById(id);
    }

    public boolean existsById(String id)
    {
        return repository.existsById(id);
    }

    public List<User> findAll()
    {
        return (List<User>) repository.findAll();
    }

    public User save(User user)
    {
        return repository.save(user);
    }
}
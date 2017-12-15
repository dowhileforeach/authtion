package ru.dwfe.net.authtion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.dao.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@Transactional(readOnly = true)
public class UserService implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        return repository.findByEmail(email).orElseThrow(() -> {
            String str = String.format("The user doesn't exist: %s", email);
            log.error(str);
            return new UsernameNotFoundException(str);
        });
    }

    public Optional<User> findById(Long id)
    {
        return repository.findById(id);
    }

    public Optional<User> findByEmail(String email)
    {
        return repository.findByEmail(email);
    }

    public boolean existsByEmail(String email)
    {
        return findByEmail(email).isPresent();
    }

    public List<User> findAll()
    {
        return (List<User>) repository.findAll();
    }

    @Transactional
    public User save(User user)
    {
        return repository.save(user);
    }

    @Transactional
    public void delete(User user)
    {
        repository.delete(user);
    }
}
package ru.dwfe.authtion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.dwfe.authtion.dao.User;
import ru.dwfe.authtion.dao.repository.UserRepository;

import java.util.List;

@Service
public class UserService implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException
    {
        return userRepository.findById(id).orElseThrow(() -> {
            String str = String.format("The user doesn't exist: %s", id);
            log.error(str);
            return new UsernameNotFoundException(str);
        });
    }

    public List<User> findAll()
    {
        return (List<User>) userRepository.findAll();
    }
}
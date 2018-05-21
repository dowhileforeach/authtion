package ru.dwfe.net.authtion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.repository.AuthtionConsumerRepository;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@Transactional(readOnly = true)
public class AuthtionConsumerService implements UserDetailsService
{
  @Autowired
  private AuthtionConsumerRepository repository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
  {
    return repository.findByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException(String.format("Username not exist: %s", email))
    );
  }

  public Optional<AuthtionConsumer> findById(Long id)
  {
    return repository.findById(id);
  }

  public Optional<AuthtionConsumer> findByEmail(String email)
  {
    return repository.findByEmail(email);
  }

  public boolean existsByEmail(String email)
  {
    return findByEmail(email).isPresent();
  }

  public List<AuthtionConsumer> findAll()
  {
    return repository.findAll();
  }

  @Transactional
  public AuthtionConsumer save(AuthtionConsumer consumer)
  {
    return repository.save(consumer);
  }

  @Transactional
  public void delete(AuthtionConsumer consumer)
  {
    repository.delete(consumer);
  }
}
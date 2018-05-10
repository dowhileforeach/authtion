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
import ru.dwfe.net.authtion.dao.Consumer;
import ru.dwfe.net.authtion.dao.repository.ConsumerRepository;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@Transactional(readOnly = true)
public class ConsumerService implements UserDetailsService
{
  private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);

  @Autowired
  private ConsumerRepository repository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
  {
    return repository.findByEmail(email).orElseThrow(() -> {
      String str = String.format("The not exist: %s", email);
      log.error(str);
      return new UsernameNotFoundException(str);
    });
  }

  public Optional<Consumer> findById(Long id)
  {
    return repository.findById(id);
  }

  public Optional<Consumer> findByEmail(String email)
  {
    return repository.findByEmail(email);
  }

  public boolean existsByEmail(String email)
  {
    return findByEmail(email).isPresent();
  }

  public List<Consumer> findAll()
  {
    return (List<Consumer>) repository.findAll();
  }

  @Transactional
  public Consumer save(Consumer consumer)
  {
    return repository.save(consumer);
  }

  @Transactional
  public void delete(Consumer consumer)
  {
    repository.delete(consumer);
  }
}
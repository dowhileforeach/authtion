package ru.dwfe.net.authtion.dao;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "mailing_welcome_when_password_was_not_passed")
public class MailingWelcomeWhenPasswordWasNotPassed implements Comparable<MailingWelcomeWhenPasswordWasNotPassed>
{
  @Column(updatable = false, insertable = false)
  private LocalDateTime createdOn;

  @Id
  private String email;

  private String password;

  private volatile boolean sended;
  private volatile boolean maxAttemptsReached;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;

  @Transient
  AtomicInteger attempt = new AtomicInteger(0);

  public static MailingWelcomeWhenPasswordWasNotPassed of(String email, String password)
  {
    MailingWelcomeWhenPasswordWasNotPassed mailingWelcomeWhenPasswordWasNotPassed = new MailingWelcomeWhenPasswordWasNotPassed();
    mailingWelcomeWhenPasswordWasNotPassed.setEmail(email);
    mailingWelcomeWhenPasswordWasNotPassed.setPassword(password);
    return mailingWelcomeWhenPasswordWasNotPassed;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public boolean isSended()
  {
    return sended;
  }

  public void setSended(boolean sended)
  {
    this.sended = sended;
  }

  public boolean isMaxAttemptsReached()
  {
    return maxAttemptsReached;
  }

  public void setMaxAttemptsReached(boolean maxAttemptsReached)
  {
    this.maxAttemptsReached = maxAttemptsReached;
  }

  public LocalDateTime getCreatedOn()
  {
    return createdOn;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MailingWelcomeWhenPasswordWasNotPassed that = (MailingWelcomeWhenPasswordWasNotPassed) o;

    return email.equals(that.email);
  }

  @Override
  public int hashCode()
  {
    return email.hashCode();
  }

  @Override
  public int compareTo(MailingWelcomeWhenPasswordWasNotPassed o)
  {
    int result;
    if ((result = this.getCreatedOn().compareTo(o.getCreatedOn())) == 0)
      result = this.getEmail().compareTo(o.getEmail());
    return result;
  }

  @Override
  public String toString()
  {
    return "{\n" +
            " \"email\": " + email + ",\n" +
            " \"password\": \"****\"\n" +
            "}";
  }
}

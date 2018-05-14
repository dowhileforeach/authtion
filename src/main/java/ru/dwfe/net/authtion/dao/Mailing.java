package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.Util;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@IdClass(Mailing.MailingId.class)
@Table(name = "authtion_mailing")
public class Mailing implements Comparable<Mailing>
{
  @Id
  @Column(updatable = false, insertable = false)
  private LocalDateTime createdOn;

  @Id
  private int type;

  @Id
  private String email;

  private String data;
  private volatile boolean sended;
  private volatile boolean maxAttemptsReached;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;

  @Transient
  private AtomicInteger attempt = new AtomicInteger(0);

  public static Mailing of(int type, String email, String data)
  {
    Mailing mailing = new Mailing();
    mailing.setCreatedOn(LocalDateTime.now());
    mailing.setType(type);
    mailing.setEmail(email);
    mailing.setData(data);
    mailing.setSended(false);
    mailing.setMaxAttemptsReached(false);
    return mailing;
  }

  public static Mailing of(int type, String email)
  {
    return of(type, email, "");
  }

  public void clear()
  {
    data = "";
  }

  //
  //  GETTERs and SETTERs
  //

  public LocalDateTime getCreatedOn()
  {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn)
  {
    this.createdOn = createdOn;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type = type;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getData()
  {
    return data;
  }

  public void setData(String data)
  {
    this.data = data;
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

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }

  public AtomicInteger getAttempt()
  {
    return attempt;
  }


  //
  //  equals, hashCode, compareTo, toString
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Mailing mailing = (Mailing) o;

    if (type != mailing.type) return false;
    if (!createdOn.equals(mailing.createdOn)) return false;
    return email.equals(mailing.email);
  }

  @Override
  public int hashCode()
  {
    int result = createdOn.hashCode();
    result = 31 * result + type;
    result = 31 * result + email.hashCode();
    return result;
  }

  @Override
  public int compareTo(Mailing o)
  {
    int result;
    if ((result = createdOn.compareTo(o.createdOn)) == 0)
      if ((result = Integer.compare(type, o.type)) == 0)
        result = email.compareTo(o.email);
    return result;
  }

  @Override
  public String toString()
  {
    return "{\n" +
            " \"createdOn\": " + "\"" + Util.formatDateTime(createdOn) + "\",\n" +
            " \"type\": " + type + ",\n" +
            " \"email\": " + email + ",\n" +
            " \"data\": \"****\",\n" +
            " \"sended\": " + sended + ",\n" +
            " \"maxAttemptsReached\": " + maxAttemptsReached + ",\n" +
            " \"attempt\": " + attempt + ",\n" +
            " \"updatedOn\": " + "\"" + Util.formatDateTime(updatedOn) + "\"\n" +
            "}";
  }

  public static class MailingId implements Serializable
  {
    private LocalDateTime createdOn;
    private int type;
    private String email;
  }
}
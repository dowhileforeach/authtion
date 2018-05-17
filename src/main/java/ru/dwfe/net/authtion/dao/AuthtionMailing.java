package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.AuthtionUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@IdClass(AuthtionMailing.AuthtionMailingId.class)
@Table(name = "authtion_mailing")
public class AuthtionMailing implements Comparable<AuthtionMailing>
{
  @Id
  @Column(updatable = false, insertable = false)
  private LocalDateTime createdOn;

  @Id
  private int type;

  @Id
  private String email;

  private String data;
  private volatile boolean sent;
  private volatile boolean maxAttemptsReached;

  private String causeOfLastFailure;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;

  @Transient
  private AtomicInteger attempt = new AtomicInteger(0);

  public static AuthtionMailing of(int type, String email, String data)
  {
    AuthtionMailing mailing = new AuthtionMailing();
    mailing.setCreatedOn(LocalDateTime.now());
    mailing.setType(type);
    mailing.setEmail(email);
    mailing.setData(data);
    mailing.setSent(false);
    mailing.setMaxAttemptsReached(false);
    mailing.setCauseOfLastFailure("");
    return mailing;
  }

  public static AuthtionMailing of(int type, String email)
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

  public boolean isSent()
  {
    return sent;
  }

  public void setSent(boolean sent)
  {
    this.sent = sent;
  }

  public boolean isMaxAttemptsReached()
  {
    return maxAttemptsReached;
  }

  public void setMaxAttemptsReached(boolean maxAttemptsReached)
  {
    this.maxAttemptsReached = maxAttemptsReached;
  }

  public String getCauseOfLastFailure()
  {
    return causeOfLastFailure;
  }

  public void setCauseOfLastFailure(String causeOfLastFailure)
  {
    this.causeOfLastFailure = causeOfLastFailure;
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

    AuthtionMailing mailing = (AuthtionMailing) o;

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
  public int compareTo(AuthtionMailing o)
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
            " \"createdOn\": " + "\"" + AuthtionUtil.formatDateTime(createdOn) + "\",\n" +
            " \"type\": " + type + ",\n" +
            " \"email\": " + email + ",\n" +
            " \"data\": \"****\",\n" +
            " \"sent\": " + sent + ",\n" +
            " \"maxAttemptsReached\": " + maxAttemptsReached + ",\n" +
            " \"causeOfLastFailure\": \"" + causeOfLastFailure + "\",\n" +
            " \"attempt\": " + attempt + ",\n" +
            " \"updatedOn\": " + "\"" + AuthtionUtil.formatDateTime(updatedOn) + "\"\n" +
            "}";
  }

  public static class AuthtionMailingId implements Serializable
  {
    private LocalDateTime createdOn;
    private int type;
    private String email;

    @Override
    public boolean equals(Object o)
    {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      AuthtionMailingId mailingId = (AuthtionMailingId) o;

      if (type != mailingId.type) return false;
      if (!createdOn.equals(mailingId.createdOn)) return false;
      return email.equals(mailingId.email);
    }

    @Override
    public int hashCode()
    {
      int result = createdOn.hashCode();
      result = 31 * result + type;
      result = 31 * result + email.hashCode();
      return result;
    }
  }
}
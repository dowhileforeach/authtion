package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.Util;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mailing_restore_password")
public class MailingRestorePassword
{
  @Id
  private String consumer;

  private String confirmKey;
  private boolean alreadySent;


  public static MailingRestorePassword of(String email)
  {
    MailingRestorePassword confirm = new MailingRestorePassword();
    confirm.setConsumer(email);
    confirm.setConfirmKey(Util.getUniqStrBase36(30));
    confirm.setAlreadySent(false);
    return confirm;
  }

  public String getConsumer()
  {
    return consumer;
  }

  public void setConsumer(String email)
  {
    this.consumer = email;
  }

  public String getConfirmKey()
  {
    return confirmKey;
  }

  public void setConfirmKey(String confirmKey)
  {
    this.confirmKey = confirmKey;
  }

  public boolean isAlreadySent()
  {
    return alreadySent;
  }

  public void setAlreadySent(boolean alreadySent)
  {
    this.alreadySent = alreadySent;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MailingRestorePassword that = (MailingRestorePassword) o;

    return consumer.equals(that.consumer);
  }

  @Override
  public int hashCode()
  {
    return consumer.hashCode();
  }

  @Override
  public String toString()
  {
    return Util.getJSONfromObject(this);
  }
}

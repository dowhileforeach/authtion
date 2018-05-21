package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.AuthtionUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static ru.dwfe.net.authtion.util.AuthtionUtil.formatDateTime;
import static ru.dwfe.net.authtion.util.AuthtionUtil.getNickNameFromEmail;
import static ru.dwfe.net.authtion.util.AuthtionUtil.prepareStringField;

@Entity
@Table(name = "authtion_users")
public class AuthtionUser
{
  @Id
  private Long consumerId;

  private String nickName;
  private String firstName;
  private String lastName;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;

  public Long getConsumerId()
  {
    return consumerId;
  }

  public void setConsumerId(Long consumerId)
  {
    this.consumerId = consumerId;
  }

  public String getNickName()
  {
    return nickName;
  }

  public void setNickName(String nickName)
  {
    this.nickName = nickName;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
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

    AuthtionUser that = (AuthtionUser) o;

    return consumerId.equals(that.consumerId);
  }

  @Override
  public int hashCode()
  {
    return consumerId.hashCode();
  }

  @Override
  public String toString()
  {
    return "{\n" +
            " \"id\": " + consumerId + ",\n" +
            " \"nickName\": \"" + nickName + "\",\n" +
            " \"firstName\": \"" + firstName + "\",\n" +
            " \"lastName\": \"" + lastName + "\",\n" +
            " \"updatedOn\": " + "\"" + formatDateTime(updatedOn) + "\"\n" +
            "}";
  }

  public static void prepareNewUser(AuthtionUser user, AuthtionConsumer consumer)
  {
    user.setConsumerId(consumer.getId());

    String nickName = user.getNickName();
    if (nickName == null)
      nickName = getNickNameFromEmail(consumer.getEmail());
    user.setNickName(prepareStringField(nickName, 20));

    user.setFirstName(prepareStringField(user.getFirstName(), 20));
    user.setLastName(prepareStringField(user.getLastName(), 20));
  }
}

package ru.dwfe.net.authtion.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
            " \"lastName\": \"" + lastName + "\"\n" +
            "}";
  }

  public static void prepareNewConsumer(AuthtionUser user, String email)
  {
    user.setFirstName(prepareStringField(user.getFirstName(), 20));
    user.setLastName(prepareStringField(user.getLastName(), 20));

    String nickName = user.getNickName();
    if (nickName == null)
      nickName = getNickNameFromEmail(email);
    user.setNickName(prepareStringField(nickName, 20));
  }
}

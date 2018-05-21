package ru.dwfe.net.authtion.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static ru.dwfe.net.authtion.util.AuthtionUtil.*;

@Entity
@Table(name = "authtion_users")
public class AuthtionUser
{
  @Id
  private Long consumerId;

  private String nickName;
  private String firstName;
  private String lastName;

  private boolean nickNameIsPublic;
  private boolean firstNameIsPublic;
  private boolean lastNameIsPublic;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;


  //
  //  GETTERs and SETTERs
  //

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

  public boolean isNickNameIsPublic()
  {
    return nickNameIsPublic;
  }

  public void setNickNameIsPublic(boolean nickNameIsPublic)
  {
    this.nickNameIsPublic = nickNameIsPublic;
  }

  public boolean isFirstNameIsPublic()
  {
    return firstNameIsPublic;
  }

  public void setFirstNameIsPublic(boolean firstNameIsPublic)
  {
    this.firstNameIsPublic = firstNameIsPublic;
  }

  public boolean isLastNameIsPublic()
  {
    return lastNameIsPublic;
  }

  public void setLastNameIsPublic(boolean lastNameIsPublic)
  {
    this.lastNameIsPublic = lastNameIsPublic;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }


  //
  //  equals, hashCode, toString
  //

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
    return toStringWithPublicCheck(true);
  }

  public String toStringWithPublicCheck(boolean onPublic)
  {
    ArrayList<String> list = new ArrayList<>();

    list.add("\"id\": " + consumerId);

    if (onPublic)
    {
      if (nickNameIsPublic)
        list.add("\"nickName\": \"" + nickName + "\"");
      if (firstNameIsPublic)
        list.add("\"firstName\": \"" + firstName + "\"");
      if (lastNameIsPublic)
        list.add(" \"lastName\": \"" + lastName + "\"");
    }
    else
    {
      list.add(getDBvalueToStringWithIsPublicInfo("nickName", nickName, nickNameIsPublic));
      list.add(getDBvalueToStringWithIsPublicInfo("firstName", firstName, firstNameIsPublic));
      list.add(getDBvalueToStringWithIsPublicInfo("lastName", lastName, lastNameIsPublic));
      list.add("\"updatedOn\": " + "\"" + formatDateTime(updatedOn) + "\"");
    }
    return "{" + list.stream().collect(Collectors.joining(",")) + "}";
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

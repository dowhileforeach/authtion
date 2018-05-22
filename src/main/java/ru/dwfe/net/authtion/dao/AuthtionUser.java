package ru.dwfe.net.authtion.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

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
  private String middleName;
  private String lastName;

  private boolean nickNameNonPublic;
  private boolean firstNameNonPublic;
  private boolean middleNameNonPublic;
  private boolean lastNameNonPublic;

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

  public String getMiddleName()
  {
    return middleName;
  }

  public void setMiddleName(String middleName)
  {
    this.middleName = middleName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

  public boolean isNickNameNonPublic()
  {
    return nickNameNonPublic;
  }

  public void setNickNameNonPublic(boolean nickNameNonPublic)
  {
    this.nickNameNonPublic = nickNameNonPublic;
  }

  public boolean isFirstNameNonPublic()
  {
    return firstNameNonPublic;
  }

  public void setFirstNameNonPublic(boolean firstNameNonPublic)
  {
    this.firstNameNonPublic = firstNameNonPublic;
  }

  public boolean isMiddleNameNonPublic()
  {
    return middleNameNonPublic;
  }

  public void setMiddleNameNonPublic(boolean middleNameNonPublic)
  {
    this.middleNameNonPublic = middleNameNonPublic;
  }

  public boolean isLastNameNonPublic()
  {
    return lastNameNonPublic;
  }

  public void setLastNameNonPublic(boolean lastNameNonPublic)
  {
    this.lastNameNonPublic = lastNameNonPublic;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }


  //
  //  equals, hashCode
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


  //
  //  UTILs
  //

  public static void prepareNewUser(AuthtionUser user, AuthtionConsumer consumer)
  {
    user.setConsumerId(consumer.getId());

    String nickName = user.getNickName();
    if (nickName == null)
      nickName = getNickNameFromEmail(consumer.getEmail());
    user.setNickName(prepareStringField(nickName, 20));

    user.setFirstName(prepareStringField(user.getFirstName(), 20));
    user.setMiddleName(prepareStringField(user.getMiddleName(), 20));
    user.setLastName(prepareStringField(user.getLastName(), 20));

    user.setNickNameNonPublic(true);
    user.setFirstNameNonPublic(true);
    user.setMiddleNameNonPublic(true);
    user.setLastNameNonPublic(true);
  }
}

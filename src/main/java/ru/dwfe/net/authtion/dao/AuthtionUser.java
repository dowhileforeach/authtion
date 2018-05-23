package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.AuthtionUtil.ReqCreateAccount;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "authtion_users")
public class AuthtionUser
{
  @Id
  private Long consumerId;

  private String nickName;
  private boolean nickNameNonPublic;

  private String firstName;
  private boolean firstNameNonPublic;

  private String middleName;
  private boolean middleNameNonPublic;

  private String lastName;
  private boolean lastNameNonPublic;

  private Integer gender;
  private boolean genderNonPublic;

  private LocalDate dateOfBirth;
  private boolean dateOfBirthNonPublic;

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

  public boolean isNickNameNonPublic()
  {
    return nickNameNonPublic;
  }

  public void setNickNameNonPublic(boolean nickNameNonPublic)
  {
    this.nickNameNonPublic = nickNameNonPublic;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  public boolean isFirstNameNonPublic()
  {
    return firstNameNonPublic;
  }

  public void setFirstNameNonPublic(boolean firstNameNonPublic)
  {
    this.firstNameNonPublic = firstNameNonPublic;
  }

  public String getMiddleName()
  {
    return middleName;
  }

  public void setMiddleName(String middleName)
  {
    this.middleName = middleName;
  }

  public boolean isMiddleNameNonPublic()
  {
    return middleNameNonPublic;
  }

  public void setMiddleNameNonPublic(boolean middleNameNonPublic)
  {
    this.middleNameNonPublic = middleNameNonPublic;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

  public boolean isLastNameNonPublic()
  {
    return lastNameNonPublic;
  }

  public void setLastNameNonPublic(boolean lastNameNonPublic)
  {
    this.lastNameNonPublic = lastNameNonPublic;
  }

  public Integer getGender()
  {
    return gender;
  }

  public void setGender(Integer gender)
  {
    this.gender = gender;
  }

  public boolean isGenderNonPublic()
  {
    return genderNonPublic;
  }

  public void setGenderNonPublic(boolean genderNonPublic)
  {
    this.genderNonPublic = genderNonPublic;
  }

  public LocalDate getDateOfBirth()
  {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth)
  {
    this.dateOfBirth = dateOfBirth;
  }

  public boolean isDateOfBirthNonPublic()
  {
    return dateOfBirthNonPublic;
  }

  public void setDateOfBirthNonPublic(boolean dateOfBirthNonPublic)
  {
    this.dateOfBirthNonPublic = dateOfBirthNonPublic;
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

  public static void prepareNewUser(AuthtionUser user, AuthtionConsumer consumer, ReqCreateAccount req)
  {
    user.setConsumerId(consumer.getId());

    String nickName = req.nickName;
    if (nickName == null)
      nickName = getNickNameFromEmail(consumer.getEmail());
    user.setNickName(prepareStringField(nickName, 20));
    user.setNickNameNonPublic(true);

    user.setFirstName(prepareStringField(req.firstName, 20));
    user.setFirstNameNonPublic(true);

    user.setMiddleName(prepareStringField(req.middleName, 20));
    user.setMiddleNameNonPublic(true);

    user.setLastName(prepareStringField(req.lastName, 20));
    user.setLastNameNonPublic(true);

    if (req.gender == null)
      user.setGender(0);
    user.setGenderNonPublic(true);

    user.setDateOfBirth(req.dateOfBirth);
    user.setDateOfBirthNonPublic(true);
  }

  private static String getNickNameFromEmail(String email)
  {
    return email.substring(0, email.indexOf('@'));
  }

  private static String prepareStringField(String field, int maxLength)
  {
    String result;

    if (field == null)
      result = "";
    else if (field.length() > maxLength)
      result = field.substring(0, maxLength - 1);
    else
      result = field;

    return result;
  }
}

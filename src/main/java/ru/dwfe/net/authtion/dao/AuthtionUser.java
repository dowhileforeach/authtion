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
  private Boolean nickNameNonPublic;

  private String firstName;
  private Boolean firstNameNonPublic;

  private String middleName;
  private Boolean middleNameNonPublic;

  private String lastName;
  private Boolean lastNameNonPublic;

  private Integer gender;
  private Boolean genderNonPublic;

  private LocalDate dateOfBirth;
  private Boolean dateOfBirthNonPublic;

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

  public Boolean getNickNameNonPublic()
  {
    return nickNameNonPublic;
  }

  public void setNickNameNonPublic(Boolean nickNameNonPublic)
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

  public Boolean getFirstNameNonPublic()
  {
    return firstNameNonPublic;
  }

  public void setFirstNameNonPublic(Boolean firstNameNonPublic)
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

  public Boolean getMiddleNameNonPublic()
  {
    return middleNameNonPublic;
  }

  public void setMiddleNameNonPublic(Boolean middleNameNonPublic)
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

  public Boolean getLastNameNonPublic()
  {
    return lastNameNonPublic;
  }

  public void setLastNameNonPublic(Boolean lastNameNonPublic)
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

  public Boolean getGenderNonPublic()
  {
    return genderNonPublic;
  }

  public void setGenderNonPublic(Boolean genderNonPublic)
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

  public Boolean getDateOfBirthNonPublic()
  {
    return dateOfBirthNonPublic;
  }

  public void setDateOfBirthNonPublic(Boolean dateOfBirthNonPublic)
  {
    this.dateOfBirthNonPublic = dateOfBirthNonPublic;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }

  public void setUpdatedOn(LocalDateTime updatedOn)
  {
    this.updatedOn = updatedOn;
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

  public static AuthtionUser of(String nickName, boolean nickNameNonPublic,
                                String firstName, boolean firstNameNonPublic,
                                String middleName, boolean middleNameNonPublic,
                                String lastName, boolean lastNameNonPublic,
                                Integer gender, boolean genderNonPublic,
                                LocalDate dateOfBirth, boolean dateOfBirthNonPublic)
  {
    AuthtionUser user = new AuthtionUser();

    user.nickName = nickName;
    user.nickNameNonPublic = nickNameNonPublic;
    user.firstName = firstName;
    user.firstNameNonPublic = firstNameNonPublic;
    user.middleName = middleName;
    user.middleNameNonPublic = middleNameNonPublic;
    user.lastName = lastName;
    user.lastNameNonPublic = lastNameNonPublic;
    user.gender = gender;
    user.genderNonPublic = genderNonPublic;
    user.dateOfBirth = dateOfBirth;
    user.dateOfBirthNonPublic = dateOfBirthNonPublic;
    user.updatedOn = LocalDateTime.now();

    return user;
  }

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

    user.setGender(req.gender == null ? 0 : req.gender);
    user.setGenderNonPublic(true);

    user.setDateOfBirth(req.dateOfBirth);
    user.setDateOfBirthNonPublic(true);
  }

  public static String getNickNameFromEmail(String email)
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

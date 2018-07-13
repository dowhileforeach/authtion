package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.AuthtionUtil.ReqCreateAccount;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static ru.dwfe.net.authtion.util.AuthtionUtil.prepareStringField;

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

  private String gender;
  private Boolean genderNonPublic;

  private LocalDate dateOfBirth;
  private Boolean dateOfBirthNonPublic;

  private String country;
  private Boolean countryNonPublic;

  private String city;
  private Boolean cityNonPublic;

  private String company;
  private Boolean companyNonPublic;

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

  public String getGender()
  {
    return gender;
  }

  public void setGender(String gender)
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

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public Boolean getCountryNonPublic()
  {
    return countryNonPublic;
  }

  public void setCountryNonPublic(Boolean countryNonPublic)
  {
    this.countryNonPublic = countryNonPublic;
  }

  public String getCity()
  {
    return city;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  public Boolean getCityNonPublic()
  {
    return cityNonPublic;
  }

  public void setCityNonPublic(Boolean cityNonPublic)
  {
    this.cityNonPublic = cityNonPublic;
  }

  public String getCompany()
  {
    return company;
  }

  public void setCompany(String company)
  {
    this.company = company;
  }

  public Boolean getCompanyNonPublic()
  {
    return companyNonPublic;
  }

  public void setCompanyNonPublic(Boolean companyNonPublic)
  {
    this.companyNonPublic = companyNonPublic;
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

    var that = (AuthtionUser) o;

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
                                String gender, boolean genderNonPublic,
                                LocalDate dateOfBirth, boolean dateOfBirthNonPublic,
                                String country, boolean countryNonPublic,
                                String city, boolean cityNonPublic,
                                String company, boolean companyNonPublic)
  {
    var user = new AuthtionUser();

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
    user.country = country;
    user.countryNonPublic = countryNonPublic;
    user.city = city;
    user.cityNonPublic = cityNonPublic;
    user.company = company;
    user.companyNonPublic = companyNonPublic;
    user.updatedOn = LocalDateTime.now();

    return user;
  }

  public static void prepareNewUser(AuthtionUser user, AuthtionConsumer consumer, ReqCreateAccount req)
  {
    user.setConsumerId(consumer.getId());

    var nickName = req.nickName;
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

    user.setGender(req.gender);
    user.setGenderNonPublic(true);

    user.setDateOfBirth(req.dateOfBirth);
    user.setDateOfBirthNonPublic(true);

    user.setCountry(req.country);
    user.setCountryNonPublic(true);

    user.setCity(prepareStringField(req.city, 100));
    user.setCityNonPublic(true);

    user.setCompany(prepareStringField(req.company, 100));
    user.setCompanyNonPublic(true);
  }

  public static String getNickNameFromEmail(String email)
  {
    return email.substring(0, email.indexOf('@'));
  }
}

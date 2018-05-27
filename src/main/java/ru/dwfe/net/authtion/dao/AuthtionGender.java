package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.dao.repository.AuthtionGenderRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "authtion_genders")
public class AuthtionGender
{
  @Id
  private String gender;
  private String description;


  //
  //  GETTERs and SETTERs
  //

  public String getGender()
  {
    return gender;
  }

  public void setGender(String gender)
  {
    this.gender = gender;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }


  //
  //  equals, hashCode
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    var that = (AuthtionGender) o;

    return gender.equals(that.gender);
  }

  @Override
  public int hashCode()
  {
    return gender.hashCode();
  }


  //
  //  UTILs
  //

  public static boolean canUseGender(String gender, AuthtionGenderRepository genderRepository, List<String> errorCodes)
  {
    if (gender != null && !genderRepository.findById(gender).isPresent())
      errorCodes.add("invalid-gender");
    return errorCodes.size() == 0;
  }
}

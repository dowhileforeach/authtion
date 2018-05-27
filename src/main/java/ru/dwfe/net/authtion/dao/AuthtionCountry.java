package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.dao.repository.AuthtionCountryRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "authtion_countries")
public class AuthtionCountry
{
  private String country;
  @Id
  private String alpha2;
  private String alpha3;
  private String phoneCode;


  //
  //  GETTERs and SETTERs
  //

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public String getAlpha2()
  {
    return alpha2;
  }

  public void setAlpha2(String alpha2)
  {
    this.alpha2 = alpha2;
  }

  public String getAlpha3()
  {
    return alpha3;
  }

  public void setAlpha3(String alpha3)
  {
    this.alpha3 = alpha3;
  }

  public String getPhoneCode()
  {
    return phoneCode;
  }

  public void setPhoneCode(String phoneCode)
  {
    this.phoneCode = phoneCode;
  }


  //
  //  equals, hashCode
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    var that = (AuthtionCountry) o;

    return alpha2.equals(that.alpha2);
  }

  @Override
  public int hashCode()
  {
    return alpha2.hashCode();
  }


  //
  //  UTILs
  //

  public static boolean canUseCountry(String country, AuthtionCountryRepository countryRepository, List<String> errorCodes)
  {
    if (country != null && !countryRepository.findById(country).isPresent())
      errorCodes.add("invalid-country");
    return errorCodes.size() == 0;
  }
}

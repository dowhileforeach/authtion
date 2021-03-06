package ru.dwfe.net.authtion.dao;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "authtion_authorities")
public class AuthtionAuthority implements GrantedAuthority
{
  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  @Id
  private String authority;

  private String description;


  public static AuthtionAuthority of(String authorityName)
  {
    AuthtionAuthority authority = new AuthtionAuthority();
    authority.setAuthority(authorityName);
    return authority;
  }


  //
  //  GETTERs and SETTERs
  //

  @Override
  public String getAuthority()
  {
    return authority;
  }

  public void setAuthority(String authority)
  {
    this.authority = authority;
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
  //  equals, hashCode, toString
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    var authority = (AuthtionAuthority) o;

    return this.authority.equals(authority.authority);
  }

  @Override
  public int hashCode()
  {
    return authority.hashCode();
  }

  @Override
  public String toString()
  {
    return "\"" + authority + "\"";
  }
}
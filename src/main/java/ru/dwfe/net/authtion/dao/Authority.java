package ru.dwfe.net.authtion.dao;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "authtion_authorities")
public class Authority implements GrantedAuthority
{
  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  @Id
  private String authority;

  private String description;


  public static Authority of(String authorityName)
  {
    Authority authority = new Authority();
    authority.setAuthority(authorityName);
    return authority;
  }

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

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Authority authority = (Authority) o;

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
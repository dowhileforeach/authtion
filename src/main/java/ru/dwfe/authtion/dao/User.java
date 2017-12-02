package ru.dwfe.authtion.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails, CredentialsContainer
{
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @Id
    @Column
    private String id;

    @JsonIgnore
    @Column
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority", referencedColumnName = "authority"))
    private Set<Authority> authorities;

    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private boolean accountNonExpired;
    @Column
    private boolean credentialsNonExpired;
    @Column
    private boolean accountNonLocked;
    @Column
    private boolean enabled;


    /*
        IMPLEMENTATION interfaces
    */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername()
    {
        return id;
    }

    @JsonIgnore
    @Override
    public String getName()
    {
        return firstName;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return accountNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return credentialsNonExpired;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return accountNonLocked;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void eraseCredentials()
    {
        password = "";
    }

    /*
        GETTERs and SETTERs
    */

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setAuthorities(Set<Authority> authorities)
    {
        this.authorities = authorities;
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

    public void setAccountNonExpired(boolean accountNonExpired)
    {
        this.accountNonExpired = accountNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired)
    {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked)
    {
        this.accountNonLocked = accountNonLocked;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /*
        equals, hashCode, toString
    */

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return "{\n" +
                " \"id\":\" " + id + "\",\n" +
                " \"password\": \"****\",\n" +
                " \"authorities\": " + authorities + ",\n" +
                " \"firstName\": \"" + firstName + "\",\n" +
                " \"lastName\": \"" + lastName + "\",\n" +
                " \"accountNonExpired\": \"" + accountNonExpired + "\",\n" +
                " \"credentialsNonExpired\": \"" + credentialsNonExpired + "\",\n" +
                " \"accountNonLocked\": \"" + accountNonLocked + "\",\n" +
                " \"enabled\": \"" + enabled + "\"\n" +
                "}";
    }
}
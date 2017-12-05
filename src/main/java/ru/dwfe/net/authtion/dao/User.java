package ru.dwfe.net.authtion.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;
import ru.dwfe.net.authtion.service.UserService;

import javax.persistence.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails, CredentialsContainer
{
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @Id
    @Column
    private String id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
    @Column
    private String confirmationKey;


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
    {   //method was overriden special for @JsonIgnore annotation
        return getUsername(); // <- don't touch!
        // The only way.
        // It affects the uniqueness of tokens:
        //   OAuth2Authentication
        //   -> AbstractAuthenticationToken
        //   -> getName()
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

    public String getConfirmationKey()
    {
        return confirmationKey;
    }

    public void setConfirmationKey(String confirmationKey)
    {
        this.confirmationKey = confirmationKey;
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


    /*
        UTILs
    */

    public static boolean canUseID(String id, UserService userService)
    {
        boolean result = false;

        if (!id.isEmpty())
        {
            if (!userService.existsById(id))
            {
                result = true;
            }
        }
        return result;
    }

    public static Map<String, String> check(User user)
    {
        Map<String, String> map = new HashMap<>();

        checkStringValue("id", user.id, map);
        checkStringValue("password", user.password, map);

        return map;
    }

    public static void prepareNewUser(User user)
    {
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(false); //is locked, will be unlocked after confirmation
        user.setEnabled(true);

        int requiredStringLength = 100;
        user.setConfirmationKey(new BigInteger(requiredStringLength * 5, new SecureRandom()).toString(36));

        Authority authority = new Authority();
        authority.setAuthority("USER");
        user.setAuthorities(Set.of(authority));

        if (user.getFirstName() == null) user.setFirstName("");
        if (user.getLastName() == null) user.setLastName("");
    }

    private static void checkStringValue(String fieldName, String value, Map<String, String> map)
    {
        String result = null;

        if (value == null) result = "required";
        else if (value.length() == 0) result = "length can't be 0";

        if (result != null)
            map.put(fieldName, result);
    }
}
package ru.dwfe.net.authtion.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.dwfe.net.authtion.service.ConfirmationKeyService;
import ru.dwfe.net.authtion.service.UserService;

import javax.persistence.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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

    public static boolean canUseID(String id, UserService userService, Map<String, Object> details)
    {
        boolean result = false;
        String fieldName = "id";
        int maxLength = 30;

        if (id != null)
        {
            if (!id.isEmpty())
            {
                if (id.length() < maxLength)
                {
                    if (!DISABLED_NAMES.contains(id))
                    {
                        if (emailRegexPattern.matcher(id).matches())
                        {
                            if (!userService.existsById(id))
                            {
                                result = true;
                            }
                            else details.put(fieldName, "user is present");
                        }
                        else details.put(fieldName, "must be valid e-mail address");
                    }
                    else details.put(fieldName, "not allowed");
                }
                else details.put(fieldName, "length must be less than " + maxLength + " characters");
            }
            else details.put(fieldName, "can't be empty");
        }
        else details.put(fieldName, "required field");

        return result;
    }

    public static boolean canUsePassword(String password, Map<String, Object> details)
    {
        boolean result = false;
        String fieldName = "password";
        int minLenght = 6;
        int maxLenght = 55;

        if (password != null)
        {
            if (!password.isEmpty())
            {
                if (password.length() >= minLenght && password.length() <= maxLenght)
                {
                    result = true;
                }
                else
                    details.put(fieldName, "length must be greater than or equal to " + minLenght + " and less than or equal to " + maxLenght);
            }
            else details.put(fieldName, "can't be empty");
        }
        else details.put(fieldName, "required field");

        return result;
    }

    public static boolean isFieldsCorrect(User user, UserService userService, Map<String, Object> details)
    {
        boolean result = false;

        if (User.canUseID(user.getId(), userService, details))
        {
            if (User.canUsePassword(user.getPassword(), details))
            {
                result = true;
            }
        }
        return result;
    }

    public static void prepareNewUser(User user, ConfirmationKeyService confirmationKeyService)
    {
        user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(user.getPassword()));

        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(false); //New user is locked. Will be unlocked after confirmation
        user.setEnabled(true);

        int requiredStringLength = 30;
        String key = new BigInteger(requiredStringLength * 5, new SecureRandom()).toString(36);
        confirmationKeyService.save(ConfirmationKey.createNewUser(user.getId(), key));

        Authority authority = new Authority();
        authority.setAuthority("USER");
        user.setAuthorities(Set.of(authority));

        if (user.getFirstName() == null) user.setFirstName("");
        if (user.getLastName() == null) user.setLastName("");
    }

    private static void checkStringValue(String fieldName, String value, Map<String, Object> map)
    {
        String result = null;

        if (value == null) result = "required";
        else if (value.length() == 0) result = "length can't be 0";

        if (result != null)
            map.put(fieldName, result);
    }

    private static final Set<String> DISABLED_NAMES = Set.of("Admin", "admin", "Administrator", "administrator");

    // http://emailregex.com/
    // RFC 5322: http://www.ietf.org/rfc/rfc5322.txt
    private static final Pattern emailRegexPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);
}
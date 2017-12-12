package ru.dwfe.net.authtion.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.dwfe.net.authtion.service.UserService;

import javax.persistence.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static ru.dwfe.net.authtion.util.Util.isDefaultCheckOK;

@Entity
@Table(name = "users")
public class User implements UserDetails, CredentialsContainer
{
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority", referencedColumnName = "authority"))
    private Set<Authority> authorities;

    @Column
    private String publicName;
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
    private boolean emailConfirmed;


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
        return email;
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

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setAuthorities(Set<Authority> authorities)
    {
        this.authorities = authorities;
    }

    public String getPublicName()
    {
        return publicName;
    }

    public void setPublicName(String publicName)
    {
        this.publicName = publicName;
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

    public boolean isEmailConfirmed()
    {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed)
    {
        this.emailConfirmed = emailConfirmed;
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
                " \"id\": " + id + ",\n" +
                " \"email\": \"" + email + "\",\n" +
                " \"password\": \"****\",\n" +
                " \"authorities\": " + authorities + ",\n" +
                " \"publicName\": \"" + publicName + "\",\n" +
                " \"firstName\": \"" + firstName + "\",\n" +
                " \"lastName\": \"" + lastName + "\",\n" +
                " \"accountNonExpired\": " + accountNonExpired + ",\n" +
                " \"credentialsNonExpired\": " + credentialsNonExpired + ",\n" +
                " \"accountNonLocked\": " + accountNonLocked + ",\n" +
                " \"enabled\": " + enabled + ",\n" +
                " \"emailConfirmed\": " + emailConfirmed + "\n" +
                "}";
    }


    /*
        UTILs
    */

    public static boolean canUseEmail(String email, UserService userService, Map<String, Object> details)
    {
        boolean result = false;
        String fieldName = "email";
        int maxLength = 50;

        if (isDefaultCheckOK(email, fieldName, details))
        {
            if (email.length() < maxLength)
            {
                if (emailRegexPattern.matcher(email).matches())
                {
                    if (!userService.existsByEmail(email))
                    {
                        result = true;
                    }
                    else details.put(fieldName, "user is present");
                }
                else details.put(fieldName, "must be valid e-mail address");
            }
            else details.put(fieldName, "length must be less than " + maxLength + " characters");
        }
        return result;
    }

    public static boolean canUsePassword(String password, String fieldName, Map<String, Object> details)
    {
        boolean result = false;
        int minLenght = 6;
        int maxLenght = 55;

        if (isDefaultCheckOK(password, fieldName, details))
        {
            if (password.length() >= minLenght && password.length() <= maxLenght)
            {
                result = true;
            }
            else details.put(fieldName, "length must be greater than or equal to "
                    + minLenght + " and less than or equal to " + maxLenght);
        }
        return result;
    }

    public static void prepareNewUser(User user)
    {
        user.setPassword(getBCryptEncodedPassword(user.getPassword()));

        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
        user.setEnabled(true);
        user.setEmailConfirmed(false);

        user.setAuthorities(Set.of(Authority.of("USER")));

        if (user.getFirstName() == null) user.setFirstName("");
        if (user.getLastName() == null) user.setLastName("");
        if (user.getPublicName() == null) user.setPublicName(user.getFirstName());
    }

    public static String getBCryptEncodedPassword(String rawPassword)
    {
        return "{bcrypt}" + new BCryptPasswordEncoder().encode(rawPassword);
    }

    public static boolean matchPassword(String type, String rawPassword, String rawEncodedPassword)
    {
        String encodedPassword = rawEncodedPassword.replace(type, "");
        return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
    }

    // http://emailregex.com/
    // RFC 5322: http://www.ietf.org/rfc/rfc5322.txt
    private static final Pattern emailRegexPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);
}
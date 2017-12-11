package ru.dwfe.net.authtion.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mailing_new_user_password")
public class MailingNewUserPassword
{
    @Id
    @Column
    private String user;

    @Column
    private String password;


    public static MailingNewUserPassword of(String user, String password)
    {
        MailingNewUserPassword mailingNewUserPassword = new MailingNewUserPassword();
        mailingNewUserPassword.setUser(user);
        mailingNewUserPassword.setPassword(password);
        return mailingNewUserPassword;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MailingNewUserPassword that = (MailingNewUserPassword) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode()
    {
        return user.hashCode();
    }

    @Override
    public String toString()
    {
        return "{\n" +
                " \"user\": " + user + ",\n" +
                " \"password\": \"****\"\n" +
                "}";
    }
}

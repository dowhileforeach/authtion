package ru.dwfe.net.authtion.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mailing_confirm_email")
public class MailingConfirmEmail
{
    @Id
    @Column
    private String user;

    @Column
    private String confirmKey;


    public static MailingConfirmEmail of(String user, String confirmKey)
    {
        MailingConfirmEmail mailingConfirmEmail = new MailingConfirmEmail();
        mailingConfirmEmail.setUser(user);
        mailingConfirmEmail.setConfirmKey(confirmKey);
        return mailingConfirmEmail;
    }


    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getConfirmKey()
    {
        return confirmKey;
    }

    public void setConfirmKey(String confirmKey)
    {
        this.confirmKey = confirmKey;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MailingConfirmEmail that = (MailingConfirmEmail) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode()
    {
        return user.hashCode();
    }
}

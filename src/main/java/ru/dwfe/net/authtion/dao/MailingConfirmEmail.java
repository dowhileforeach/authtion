package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.util.Util;

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

    @Column
    private boolean alreadySent;


    public static MailingConfirmEmail of(String user)
    {
        MailingConfirmEmail confirm = new MailingConfirmEmail();
        confirm.setUser(user);
        confirm.setConfirmKey(Util.getUniqStr(30));
        confirm.setAlreadySent(false);
        return confirm;
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

    public boolean isAlreadySent()
    {
        return alreadySent;
    }

    public void setAlreadySent(boolean alreadySent)
    {
        this.alreadySent = alreadySent;
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

    @Override
    public String toString()
    {
        return Util.getJSONfromObject(this);
    }
}

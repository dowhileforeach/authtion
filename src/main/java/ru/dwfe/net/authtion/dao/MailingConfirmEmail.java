package ru.dwfe.net.authtion.dao;

import ru.dwfe.net.authtion.Util;

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
    private Long user;

    @Column
    private String confirmKey;


    public static MailingConfirmEmail of(Long user)
    {
        MailingConfirmEmail confirm = new MailingConfirmEmail();
        confirm.setUser(user);
        confirm.setConfirmKey(Util.getUniqStr(30));
        return confirm;
    }

    public Long getUser()
    {
        return user;
    }

    public void setUser(Long user)
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

    @Override
    public String toString()
    {
        return Util.getJSONfromObject(this);
    }
}

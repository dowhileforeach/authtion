package ru.dwfe.net.authtion.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "confirmation_key")
public class ConfirmationKey
{
    @Column(name = "confirm_key")
    private String key;

    @Id
    @Column
    private String user;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfirmationKey that = (ConfirmationKey) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
    }
}

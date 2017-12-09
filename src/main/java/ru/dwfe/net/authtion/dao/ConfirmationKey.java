package ru.dwfe.net.authtion.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "confirmation_key")
public class ConfirmationKey
{
    @Id
    @Column
    private String user;

    @Column(name = "confirm_key")
    private String key;

    @Column
    private boolean createNewUser;

    @Column
    private boolean restoreUserPass;

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

    public boolean isCreateNewUser()
    {
        return createNewUser;
    }

    public void setCreateNewUser(boolean createNewUser)
    {
        this.createNewUser = createNewUser;
    }

    public boolean isRestoreUserPass()
    {
        return restoreUserPass;
    }

    public void setRestoreUserPass(boolean restoreUserPass)
    {
        this.restoreUserPass = restoreUserPass;
    }

    public static ConfirmationKey of(String user, String key, boolean createNewUser, boolean restoreUserPass)
    {
        ConfirmationKey confirmationKey = new ConfirmationKey();
        confirmationKey.setUser(user);
        confirmationKey.setKey(key);

        if (createNewUser)
            confirmationKey.setCreateNewUser(true);
        else if (restoreUserPass)
            confirmationKey.setRestoreUserPass(true);

        return confirmationKey;
    }

    public static ConfirmationKey createNewUser(String user, String key)
    {
        return of(user, key, true, false);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfirmationKey that = (ConfirmationKey) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode()
    {
        return user.hashCode();
    }
}

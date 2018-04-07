package ru.dwfe.net.authtion.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mailing_welcome_when_password_was_not_passed")
public class MailingWelcomeWhenPasswordWasNotPassed
{
    @Id
    private String consumer;

    private String password;


    public static MailingWelcomeWhenPasswordWasNotPassed of(String email, String password)
    {
        MailingWelcomeWhenPasswordWasNotPassed mailingWelcomeWhenPasswordWasNotPassed = new MailingWelcomeWhenPasswordWasNotPassed();
        mailingWelcomeWhenPasswordWasNotPassed.setConsumer(email);
        mailingWelcomeWhenPasswordWasNotPassed.setPassword(password);
        return mailingWelcomeWhenPasswordWasNotPassed;
    }

    public String getConsumer()
    {
        return consumer;
    }

    public void setConsumer(String email)
    {
        this.consumer = email;
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

        MailingWelcomeWhenPasswordWasNotPassed that = (MailingWelcomeWhenPasswordWasNotPassed) o;

        return consumer.equals(that.consumer);
    }

    @Override
    public int hashCode()
    {
        return consumer.hashCode();
    }

    @Override
    public String toString()
    {
        return "{\n" +
                " \"email\": " + consumer + ",\n" +
                " \"password\": \"****\"\n" +
                "}";
    }
}

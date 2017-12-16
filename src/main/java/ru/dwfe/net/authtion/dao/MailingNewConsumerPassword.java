package ru.dwfe.net.authtion.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mailing_new_consumer_password")
public class MailingNewConsumerPassword
{
    @Id
    private String consumer;

    private String password;


    public static MailingNewConsumerPassword of(String email, String password)
    {
        MailingNewConsumerPassword mailingNewConsumerPassword = new MailingNewConsumerPassword();
        mailingNewConsumerPassword.setConsumer(email);
        mailingNewConsumerPassword.setPassword(password);
        return mailingNewConsumerPassword;
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

        MailingNewConsumerPassword that = (MailingNewConsumerPassword) o;

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

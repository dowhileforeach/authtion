package ru.dwfe.net.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import ru.dwfe.net.authtion.dao.Consumer;
import ru.dwfe.net.authtion.dao.MailingConfirmConsumerEmail;
import ru.dwfe.net.authtion.dao.MailingNewConsumerPassword;
import ru.dwfe.net.authtion.dao.MailingRestoreConsumerPassword;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmConsumerEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingNewConsumerPasswordRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestoreConsumerPasswordRepository;
import ru.dwfe.net.authtion.service.ConsumerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.Global.*;
import static ru.dwfe.net.authtion.dao.Consumer.*;
import static ru.dwfe.net.authtion.util.Util.*;

@RestController
public class AppControllerV1
{
    private static final String API = "/v1";

    @Autowired
    ConsumerService consumerService;

    @Autowired
    MailingNewConsumerPasswordRepository mailingNewConsumerPasswordRepository;
    @Autowired
    MailingConfirmConsumerEmailRepository mailingConfirmConsumerEmailRepository;
    @Autowired
    MailingRestoreConsumerPasswordRepository mailingRestoreConsumerPasswordRepository;

    @RequestMapping(API + resource_public)
    public String publicResource()
    {
        return "{\"public\": true}";
    }

    @RequestMapping(API + resource_cities)
    @PreAuthorize("hasAuthority('USER')")
    public String cities()
    {
        return "{\"cities\": true}";
    }

    @RequestMapping(API + resource_users)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Consumer> users()
    {
        return consumerService.findAll();
    }

    @RequestMapping(value = API + resource_checkConsumerEmail, method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkConsumerEmail(@RequestBody String body)
    {
        boolean result;
        Map<String, Object> details = new HashMap<>();

        String email = (String) getValueFromJSON(body, "email");
        result = canUseEmail(email, consumerService, details);

        return getResponse("canUse", result, details);
    }

    @RequestMapping(value = API + resource_checkConsumerPass, method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkConsumerPass(@RequestBody String body)
    {
        boolean result;
        Map<String, Object> details = new HashMap<>();

        String password = (String) getValueFromJSON(body, "password");
        result = canUsePassword(password, "password", details);

        return getResponse("canUse", result, details);
    }

    @RequestMapping(value = API + resource_createConsumer, method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String createConsumer(@RequestBody Consumer consumer)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();
        String automaticallyGeneratedPassword = "";

        if (canUseEmail(consumer.getEmail(), consumerService, details))
        {
            String receivedPassword = consumer.getPassword();
            if (receivedPassword == null)
            {   //if password wasn't passed
                automaticallyGeneratedPassword = getUniqStr(10);
                consumer.setPassword(automaticallyGeneratedPassword);
            }
            else
            {  //if password was passed
                canUsePassword(receivedPassword, "password", details);
            }
        }
        if (details.size() == 0)
        {
            //prepare
            prepareNewConsumer(consumer);

            //put consumer to the database
            consumerService.save(consumer);

            if (!automaticallyGeneratedPassword.isEmpty())
            { //If the password was not passed, then it is necessary to send an automatically generated password to the consumer
                mailingNewConsumerPasswordRepository
                        .save(MailingNewConsumerPassword.of(consumer.getEmail(), automaticallyGeneratedPassword));

                //TODO send e-mail
            }
            result = true;
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + resource_updateConsumer, method = POST)
    @PreAuthorize("hasAuthority('USER')")
    public String updateConsumer(@RequestBody String body, OAuth2Authentication authentication)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        final String WARNING = "warning";
        final String NO_CHANGES_FOUND = "no changes found";
        final String CHANGE_SAVED = "change saved";

        final String NICKNAME_FIELD = "nickName";
        final String FIRSTNAME_FIELD = "firstName";
        final String LASTNAME_FIELD = "lastName";

        if (map.size() > 0)
        {
            Consumer consumerAuth = (Consumer) authentication.getPrincipal();

            String nickName = (String) getValue(map, NICKNAME_FIELD);
            String firstName = (String) getValue(map, FIRSTNAME_FIELD);
            String lastName = (String) getValue(map, LASTNAME_FIELD);

            boolean isNickName = nickName != null;
            boolean isFirstName = firstName != null;
            boolean isLastName = lastName != null;

            if (isNickName || isFirstName || isLastName)
            {
                boolean wasModified = false;
                Consumer consumer = consumerService.findByEmail(consumerAuth.getEmail()).get();

                if (isNickName && !nickName.equals(consumer.getNickName()))
                {
                    consumer.setNickName(nickName);
                    details.put(NICKNAME_FIELD, CHANGE_SAVED);
                    wasModified = true;
                }
                if (isFirstName && !firstName.equals(consumer.getFirstName()))
                {
                    consumer.setFirstName(firstName);
                    details.put(FIRSTNAME_FIELD, CHANGE_SAVED);
                    wasModified = true;
                }
                if (isLastName && !lastName.equals(consumer.getLastName()))
                {
                    consumer.setLastName(lastName);
                    details.put(LASTNAME_FIELD, CHANGE_SAVED);
                    wasModified = true;
                }

                if (wasModified)
                {
                    consumerService.save(consumer);

                    result = true;
                }
                else details.put(WARNING, NO_CHANGES_FOUND);
            }
            else details.put(WARNING, NO_CHANGES_FOUND);
        }
        else details.put(WARNING, NO_CHANGES_FOUND);

        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + resource_getConsumerData)
    @PreAuthorize("hasAuthority('USER')")
    public String getConsumerData(OAuth2Authentication authentication)
    {
        return getResponse("success", true, authentication.getPrincipal().toString());
    }

    @RequestMapping(value = API + resource_publicConsumer + "/{id}")
    public String publicConsumer(@PathVariable Long id)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();

        Optional<Consumer> consumerById = consumerService.findById(id);
        if (consumerById.isPresent())
        {
            Consumer consumer = consumerById.get();
            details.put("id", consumer.getId());
            details.put("nickName", consumer.getNickName());
            result = true;
        }
        else details.put("error", "not exist");

        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + resource_reqConfirmConsumerEmail)
    @PreAuthorize("hasAuthority('USER')")
    public String requestConfirmEmail(OAuth2Authentication authentication)
    {
        String email = ((Consumer) authentication.getPrincipal()).getEmail();
        mailingConfirmConsumerEmailRepository.save(MailingConfirmConsumerEmail.of(email));

        //TODO send e-mail

        return getResponse("success", true, Map.of());
    }

    @RequestMapping(API + resource_confirmConsumerEmail)
    public String confirmConsumerEmail(@RequestParam String key)
    {
        boolean result = false;
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();

        if (isDefaultCheckOK(key, fieldName, details))
        {
            Optional<MailingConfirmConsumerEmail> confirmByKey = mailingConfirmConsumerEmailRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                MailingConfirmConsumerEmail confirm = confirmByKey.get();

                //The Consumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
                Consumer consumer = consumerService.findByEmail(confirm.getConsumer()).get();
                consumer.setEmailConfirmed(true); //Now email is confirmed
                consumerService.save(consumer);

                //delete this confirmation key from database
                mailingConfirmConsumerEmailRepository.delete(confirm);

                result = true;
            }
            else details.put(fieldName, "key does not exist");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + resource_changeConsumerPass, method = POST)
    @PreAuthorize("hasAuthority('USER')")
    public String changeConsumerPass(@RequestBody String body, OAuth2Authentication authentication)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        String oldpass = (String) getValue(map, "oldpass");
        String newpass = (String) getValue(map, "newpass");

        if (isDefaultCheckOK(oldpass, "oldpass", details)
                && canUsePassword(newpass, "newpass", details))
        {
            Long id = ((Consumer) authentication.getPrincipal()).getId();
            Consumer consumer = consumerService.findById(id).get();
            if (matchPassword("{bcrypt}", oldpass, consumer.getPassword()))
            {
                consumer.setPassword(getBCryptEncodedPassword(newpass));
                consumerService.save(consumer);

                result = true;
            }
            else details.put("oldpass", "wrong");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + resource_reqRestoreConsumerPass, method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String reqRestoreConsumerPass(@RequestBody String body)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();

        String email = (String) getValueFromJSON(body, "email");

        if (isDefaultEmailCheckOK(email, details))
        {
            if (consumerService.existsByEmail(email))
            {
                MailingRestoreConsumerPassword confirm = MailingRestoreConsumerPassword.of(email);
                mailingRestoreConsumerPasswordRepository.save(confirm);

                //TODO send e-mail

                result = true;
            }
            else details.put("error", "not exist");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(API + resource_confirmRestoreConsumerPass)
    public String confirmRestoreConsumerPass(@RequestParam String key)
    {
        boolean result = false;
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();

        if (isDefaultCheckOK(key, fieldName, details))
        {
            Optional<MailingRestoreConsumerPassword> confirmByKey = mailingRestoreConsumerPasswordRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                details.put("email", confirmByKey.get().getConsumer());
                details.put("key", key);
                result = true;
            }
            else details.put(fieldName, "key does not exist");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + resource_restoreConsumerPass, method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String restoreConsumerPass(@RequestBody String body)
    {
        boolean result = false;
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        String email = (String) getValue(map, "email");
        String key = (String) getValue(map, "key");
        String newpass = (String) getValue(map, "newpass");

        if (canUsePassword(newpass, "newpass", details)
                && isDefaultCheckOK(key, "key", details)
                && isDefaultEmailCheckOK(email, details))
        {
            Optional<MailingRestoreConsumerPassword> confirmByKey = mailingRestoreConsumerPasswordRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                MailingRestoreConsumerPassword confirm = confirmByKey.get();
                if (email.equals(confirm.getConsumer()))
                {
                    //The Consumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
                    Consumer consumer = consumerService.findByEmail(email).get();
                    consumer.setPassword(getBCryptEncodedPassword(newpass));
                    consumerService.save(consumer);

                    mailingRestoreConsumerPasswordRepository.delete(confirm);

                    result = true;
                }
                else details.put(fieldName, "email from request doesn't match with email associated with key");
            }
            else details.put(fieldName, "key does not exist");
        }
        return getResponse("success", result, details);
    }
}

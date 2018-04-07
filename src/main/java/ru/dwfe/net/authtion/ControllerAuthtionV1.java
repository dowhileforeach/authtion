package ru.dwfe.net.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.*;
import ru.dwfe.net.authtion.dao.Consumer;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;
import ru.dwfe.net.authtion.dao.MailingWelcomeWhenPasswordWasNotPassed;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingWelcomeWhenPasswordWasNotPassedRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestorePasswordRepository;
import ru.dwfe.net.authtion.service.ConsumerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.dwfe.net.authtion.Global.*;
import static ru.dwfe.net.authtion.dao.Consumer.*;
import static ru.dwfe.net.authtion.util.Util.*;

@RestController
@RequestMapping(path = API_V1, produces = "application/json; charset=utf-8")
public class ControllerAuthtionV1
{
    @Autowired
    ConsumerService consumerService;

    @Autowired
    MailingWelcomeWhenPasswordWasNotPassedRepository mailingWelcomeWhenPasswordWasNotPassedRepository;
    @Autowired
    MailingConfirmEmailRepository mailingConfirmEmailRepository;
    @Autowired
    MailingRestorePasswordRepository mailingRestorePasswordRepository;

    @Autowired
    ConsumerTokenServices tokenServices;

    @PostMapping(resource_checkConsumerEmail)
    public String checkConsumerEmail(@RequestBody String body)
    {
        Map<String, Object> details = new HashMap<>();

        String email = (String) getValueFromJSON(body, "email");
        canUseEmail(email, consumerService, details);

        return getResponse("success", details);
    }

    @PostMapping(resource_checkConsumerPass)
    public String checkConsumerPass(@RequestBody String body)
    {
        Map<String, Object> details = new HashMap<>();

        String password = (String) getValueFromJSON(body, "password");
        canUsePassword(password, "password", details);

        return getResponse("success", details);
    }

    @PostMapping(resource_createConsumer)
    public String createConsumer(@RequestBody Consumer consumer)
    {
        Map<String, Object> details = new HashMap<>();

        String password = consumer.getPassword();
        String automaticallyGeneratedPassword = "";

        if (canUseEmail(consumer.getEmail(), consumerService, details))
            if (password == null)
            { //if password wasn't passed
                automaticallyGeneratedPassword = getUniqStr(10);
                password = automaticallyGeneratedPassword;
            }
            else //if password was passed
                canUsePassword(password, "password", details);

        if (details.size() == 0)
        {   //prepare
            setNewPassword(consumer, password);
            prepareNewConsumer(consumer);

            //put consumer to the database
            consumerService.save(consumer);

            if (automaticallyGeneratedPassword.isEmpty())
            {
                //TODO: service alert #1
            }
            else
            { //if the password was not passed, then it is necessary to send an automatically generated password to the new consumer
                mailingWelcomeWhenPasswordWasNotPassedRepository
                        .save(MailingWelcomeWhenPasswordWasNotPassed.of(consumer.getEmail(), automaticallyGeneratedPassword));

                //TODO: service alert #2
                //      set Consumer field 'email_confirmed' to true
            }
        }
        return getResponse("success", details);
    }

    @PostMapping(resource_updateConsumer)
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
                else
                    details.put(WARNING, NO_CHANGES_FOUND);
            }
            else details.put(WARNING, NO_CHANGES_FOUND);
        }
        else details.put(WARNING, NO_CHANGES_FOUND);

        return getResponse("success", result, details);
    }

    @GetMapping(resource_getConsumerData)
    @PreAuthorize("hasAuthority('USER')")
    public String getConsumerData(OAuth2Authentication authentication)
    {
        return getResponse("success", true, authentication.getPrincipal().toString());
    }

    @GetMapping(resource_publicConsumer + "/{id}")
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

    @GetMapping(resource_listOfConsumers)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Consumer> users()
    {
        return consumerService.findAll();
    }

    @GetMapping(resource_reqConfirmConsumerEmail)
    @PreAuthorize("hasAuthority('USER')")
    public String requestConfirmEmail(OAuth2Authentication authentication)
    {
        String email = ((Consumer) authentication.getPrincipal()).getEmail();
        mailingConfirmEmailRepository.save(MailingConfirmEmail.of(email));

        //TODO: service alert #3

        return getResponse("success", Map.of());
    }

    @GetMapping(resource_confirmConsumerEmail)
    public String confirmConsumerEmail(@RequestParam(required = false) String key)
    {
        String fieldName = "key";
        Map<String, Object> details = new HashMap<>();

        if (isDefaultCheckOK(key, fieldName, details))
        {
            Optional<MailingConfirmEmail> confirmByKey = mailingConfirmEmailRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                MailingConfirmEmail confirm = confirmByKey.get();

                //The Consumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
                Consumer consumer = consumerService.findByEmail(confirm.getConsumer()).get();
                consumer.setEmailConfirmed(true); //Now email is confirmed
                consumerService.save(consumer);

                //delete this confirmation key from database
                mailingConfirmEmailRepository.delete(confirm);
            }
            else details.put(fieldName, "does not exist");
        }
        return getResponse("success", details);
    }

    @PostMapping(resource_changeConsumerPass)
    @PreAuthorize("hasAuthority('USER')")
    public String changeConsumerPass(@RequestBody String body, OAuth2Authentication authentication)
    {
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        String oldpass = (String) getValue(map, "oldpass");
        String newpass = (String) getValue(map, "newpass");

        if (isDefaultCheckOK(oldpass, "oldpass", details)
                && canUsePassword(newpass, "newpass", details))
        {
            Long id = ((Consumer) authentication.getPrincipal()).getId();
            Consumer consumer = consumerService.findById(id).get();
            if (matchPassword(oldpass, consumer.getPassword()))
            {
                setNewPassword(consumer, newpass);
                consumerService.save(consumer);

                //TODO: service alert #4
            }
            else details.put("oldpass", "wrong");
        }
        return getResponse("success", details);
    }

    @PostMapping(resource_reqRestoreConsumerPass)
    public String reqRestoreConsumerPass(@RequestBody String body)
    {
        Map<String, Object> details = new HashMap<>();

        String email = (String) getValueFromJSON(body, "email");

        if (isEmailCheckOK(email, details))
        {
            if (consumerService.existsByEmail(email))
            {
                MailingRestorePassword confirm = MailingRestorePassword.of(email);
                mailingRestorePasswordRepository.save(confirm);

                //TODO: service alert #5
            }
            else details.put("error", "not exist");
        }
        return getResponse("success", details);
    }

    @GetMapping(resource_confirmRestoreConsumerPass)
    public String confirmRestoreConsumerPass(@RequestParam(required = false) String key)
    {
        boolean result = false;
        String fieldName = "key";
        Map<String, Object> details = new HashMap<>();

        if (isDefaultCheckOK(key, fieldName, details))
        {
            Optional<MailingRestorePassword> confirmByKey = mailingRestorePasswordRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                details.put("email", confirmByKey.get().getConsumer());
                details.put("key", key);
                result = true;
            }
            else details.put(fieldName, "does not exist");
        }
        return getResponse("success", result, details);
    }

    @PostMapping(resource_restoreConsumerPass)
    public String restoreConsumerPass(@RequestBody String body)
    {
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        String email = (String) getValue(map, "email");
        String key = (String) getValue(map, "key");
        String newpass = (String) getValue(map, "newpass");

        if (canUsePassword(newpass, "newpass", details)
                && isDefaultCheckOK(key, "key", details)
                && isEmailCheckOK(email, details))
        {
            Optional<MailingRestorePassword> confirmByKey = mailingRestorePasswordRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                MailingRestorePassword confirm = confirmByKey.get();
                if (email.equals(confirm.getConsumer()))
                {
                    //The Consumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
                    Consumer consumer = consumerService.findByEmail(email).get();
                    setNewPassword(consumer, newpass);
                    consumerService.save(consumer);

                    mailingRestorePasswordRepository.delete(confirm);
                }
                else details.put(fieldName, "email from request doesn't match with email associated with key");
            }
            else details.put(fieldName, "key does not exist");
        }
        return getResponse("success", details);
    }

    @GetMapping(resource_signOut)
    @PreAuthorize("hasAuthority('USER')")
    public String signOut(OAuth2Authentication authentication)
    {
        OAuth2AccessToken accessToken = ((AuthorizationServerTokenServices) tokenServices).getAccessToken(authentication);
        tokenServices.revokeToken(accessToken.getValue());

        return getResponse("success", Map.of());
    }
}



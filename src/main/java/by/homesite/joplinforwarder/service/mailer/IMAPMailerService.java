package by.homesite.joplinforwarder.service.mailer;

import static by.homesite.joplinforwarder.config.Constants.CONNECT_TIMEOUT;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.MailRepository;
import by.homesite.joplinforwarder.service.RulesService;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.mailer.mapper.IMAPMailMessageMapper;
import by.homesite.joplinforwarder.service.storage.StorageService;
import by.homesite.joplinforwarder.util.MailUtil;
import com.sun.mail.imap.IMAPFolder;
import io.jsonwebtoken.lang.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.util.MailSSLSocketFactory;

@Service
public class IMAPMailerService implements MailerService
{
    private final SettingsService settingsService;
    private final RulesService rulesService;
    private final MailRepository mailRepository;
    private final IMAPMailMessageMapper imapMailMessageMapper;
    private final ApplicationProperties applicationProperties;
    private final StorageService storageService;

    private static final Logger log = LoggerFactory.getLogger(IMAPMailerService.class);

    public IMAPMailerService(SettingsService settingsService,
                             RulesService rulesService,
                             MailRepository mailRepository,
                             IMAPMailMessageMapper imapMailMessageMapper,
                             ApplicationProperties applicationProperties,
                             @Qualifier("storageServiceStrategy") StorageService storageService) {
        this.settingsService = settingsService;
        this.rulesService = rulesService;
        this.mailRepository = mailRepository;
        this.imapMailMessageMapper = imapMailMessageMapper;
        this.applicationProperties = applicationProperties;
        this.storageService = storageService;
    }

    @Override
    public void getMail() {
        List<User> users = this.settingsService.getMailSettingsByUsers();
        users.forEach(user -> {
            String messageId = getLastSavedMessageId(user.getId());
            fetchAndSaveMails(user, messageId);
        });
    }

    @Override
    public void deleteOldItems(int purgeMailsPeriod) {
    }

    private void fetchAndSaveMails(User user, String lastSavedMessageId) {
        String mailServer = settingsService.getSettingValue(user.getSettingsList(), "mailserver");
        String mailPort = settingsService.getSettingValue(user.getSettingsList(), "mailport");
        String mailUser = settingsService.getSettingValue(user.getSettingsList(), "username");
        String mailPassword = settingsService.getSettingValue(user.getSettingsList(), "password");

        if (!StringUtils.hasText(mailServer) || !StringUtils.hasText(mailPort)) {
            return;
        }

        Properties properties = configureImapConnection();

        final javax.mail.Session session = javax.mail.Session.getInstance(properties);
        try
        {
            Store store = session.getStore("imaps");
            store.connect(mailServer, Integer.parseInt(mailPort), mailUser, mailPassword);

            Folder object = store.getFolder("INBOX");

            object.open(Folder.READ_WRITE);
            Message[] messes = object.getMessages();

            for (Message mess : messes)
            {
                String id =  StringUtils.hasText(((IMAPMessage) mess).getMessageID())
                        ? ((IMAPMessage) mess).getMessageID()
                        : String.valueOf(mess.getReceivedDate().getTime());
                if (!StringUtils.hasText(id) || getMailByMessageId(user, id) != null) {
                    continue;
                }
                if (id.equals(lastSavedMessageId)) {
                    break;
                }
                
                Mail mail = imapMailMessageMapper.toDto((IMAPMessage) mess);

                mail.setUser(user);
                mail.setRule(rulesService.getUserRule(mail, user));
                mail.setMessageId(id);

                if (mail.getRule() != null)
                {
                    mail = mailRepository.save(mail);
                    mail.setAttachments(saveAttachements(user, MailUtil.getFilesFromMessage(mess), mail.getId()));
                    mailRepository.save(mail);

                    storageService.storeRecord(user, mail);

                    performFinalSteps(user, mail, (IMAPMessage) mess, store);
                }
                else
                {
                    log.info(String.format("Message %s does not meet any rules for user %s", mail.getSubject(), user.getUsername()));
                }
            }

            object.close(false);

            settingsService.setSettingValue(user, "lasttime_mail_processed", String.valueOf(OffsetDateTime.now().toEpochSecond()));

            if (store.isConnected()) {
                store.close();
            }

        } catch (MessagingException e) {
            log.error(String.format("Can not get access to the user %s mailbox: %s", user.getUsername(), e.getMessage()));
        }
    }

    private void performFinalSteps(User user, Mail mail, IMAPMessage mess, Store store) {
        switch (mail.getRule().getFinal_action()) {
            case "MARK_READ" -> {
                try {
                    mess.setFlag(Flags.Flag.SEEN, true);
                } catch (MessagingException e) {
                    log.error(String.format("Can not set flag SEEN to the user %s message: %s", user.getUsername(), mail.getSubject()));
                }
            }
            case "DELETE" -> {
                try {
                    mess.setFlag(Flags.Flag.DELETED, true);
                } catch (MessagingException e) {
                    log.error(String.format("Can not set flag DELETED to the user %s message: %s", user.getUsername(), mail.getSubject()));
                }
            }
            case "MOVE_TO_FOLDER" -> {
                if (StringUtils.hasText(mail.getRule().getFinal_action_target())) {
                    try {
                        IMAPFolder object = (IMAPFolder) store.getFolder(mail.getRule().getFinal_action_target());
                        Message[] msgs = new Message[1];
                        msgs[0] = mess;
                        object.moveMessages(msgs, object);
                    } catch (MessagingException e) {
                        log.error(String.format("Can not move mail message to the user %s message: %s", user.getUsername(), mail.getSubject()));
                    }
                }
            }
        }
    }

    private String saveAttachements(User user, List<String> filesFromMessage, Integer id)
    {
        List<String> attachments = new ArrayList<>();
        if (Collections.isEmpty(filesFromMessage)) {
            return "";
        }
        
        String uploadDir = getUploadDir(user.getUsername(), String.valueOf(id));
        try
        {
            Files.createDirectories(Paths.get(uploadDir));
        }
        catch (IOException e)
        {
            log.info(String.format("Creation user %s directory error: %s", user.getUsername(), e.getMessage()));
            return "";
        }
        filesFromMessage.forEach(it -> {
            String realFileName = MailUtil.getRealFileName(it);
            try
            {
                Files.move(Path.of(it), Path.of(uploadDir + realFileName));
                attachments.add(uploadDir + realFileName);
            }
            catch (IOException e)
            {
                log.info(String.format("Moving file %s to %s error: %s", it, uploadDir + realFileName , e.getMessage()));
            }
        });

        return String.join("|", attachments);
    }
    
    private Object getMailByMessageId(User user, String id)
    {
        return mailRepository.getByUserAndMessageId(user, id);
    }

    private String getLastSavedMessageId(Long id) {
        Mail recentMail = mailRepository.findTop1ByUserIdOrderByReceivedDesc(id);
        if (recentMail != null) {
            return recentMail.getMessageId();
        }
        return "";
    }

    private String getUploadDir(String userId, String messageId) {
        return applicationProperties.getUpload().getLocalPath() + applicationProperties.getUpload().getUploadDir() + File.separator + userId + File.separator + applicationProperties.getUpload().getAttachDir() + File.separator + messageId + File.separator;
    }


    private Properties configureImapConnection()
    {

        Properties properties = new Properties();
        MailSSLSocketFactory sf;
        try
        {
            sf = new MailSSLSocketFactory();
        }
        catch (GeneralSecurityException e)
        {
            log.error("Can not get ssl certificate");
            return properties;
        }
        sf.setTrustAllHosts(true);

        properties.put("mail.imaps.ssl.socketFactory", sf);
        properties.put("mail.imaps.ssl.trust", "*");
        properties.put("mail.imaps.starttls.enable", "true");
        properties.put("mail.imaps.timeout", CONNECT_TIMEOUT);
        properties.put("mail.store.protocol", "imaps");
        return properties;
    }
}

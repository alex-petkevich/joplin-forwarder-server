package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.JoplinforwarderApplication;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.MailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.net.ConnectException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class MailService {
    private final SettingsService settingsService;
    private final RulesService rulesService;
    private final MailRepository mailRepository;

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public MailService(SettingsService settingsService, RulesService rulesService, MailRepository mailRepository) {
        this.settingsService = settingsService;
        this.rulesService = rulesService;
        this.mailRepository = mailRepository;
    }

    public void getMail() {
        List<User> users = this.settingsService.getMailSettingsByUsers();
        users.forEach(user -> {
            String messageId = getLastSavedMessageId(user.getId());
            fetchAndSaveMails(user, messageId);
        });
    }

    private void fetchAndSaveMails(User user, String messageId) {
        String mailServer = settingsService.getSettingValue(user.getSettingsList(), "mailserver");
        String mailPort = settingsService.getSettingValue(user.getSettingsList(), "mailport");
        String mailUser = settingsService.getSettingValue(user.getSettingsList(), "username");
        String mailPassword = settingsService.getSettingValue(user.getSettingsList(), "password");

        if (!StringUtils.hasText(mailServer) || !StringUtils.hasText(mailPort)) {
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.pop3.socketFactory.fallback", "false");
        properties.put("mail.pop3.ssl.protocols", "TLSv1.2");

        try {
            final javax.mail.Session session = javax.mail.Session.getInstance(properties);
            Store store = session.getStore("pop3s");
            store.connect(mailServer, Integer.parseInt(mailPort), mailUser, mailPassword);

            Folder[] personalFolders = store.getDefaultFolder().list( "*" );
            for (Folder object : personalFolders) {
                if ((object.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0){
                    object.open(Folder.READ_ONLY);
                    Message[] messes = object.getMessages();

                    for (Message mess : messes) {
                        Mail mail = new Mail();
                            mail.setSubject(mess.getSubject());
                        mail.setReceived(mess.getReceivedDate().toInstant().atOffset(ZoneOffset.UTC));
                        String recipients = Arrays.stream(mess.getAllRecipients())
                                .map(Address::getType)
                                .collect(Collectors.joining(","));
                        String sender = Arrays.stream(mess.getFrom())
                                .map(Address::getType)
                                .collect(Collectors.joining(","));
                        mail.setSender(sender);
                        mail.setRecipient(recipients);
                        mail.setMessage_id(String.valueOf(mess.getMessageNumber()));
                        mail.setText(mess.getDescription());
                        mail.setUser(user);
                        if (rulesService.meetsUserRule(mail, user)) {
                            mailRepository.save(mail);
                        } else {
                            log.info(String.format("Message %s does not meet any rules for user %s", mail.getSubject(), user.getUsername()));
                        }
                    }
                    object.close(false);
                }
            }

            if (store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
            log.error(String.format("Can not get access to the user %s mailbox: %s", user.getUsername(), e.getMessage()));
        }
        settingsService.setSettingValue(user, "lasttime_mail_processed", String.valueOf(OffsetDateTime.now().toEpochSecond()));
    }

    private String getLastSavedMessageId(Long id) {
        Mail recentMail = mailRepository.findTop1ByUserIdOrderByReceivedDesc(id);
        if (recentMail != null) {
            return recentMail.getMessage_id();
        }
        return "";
    }

    public void deleteOldItems(int purgeMailsPeriod) {
    }
}

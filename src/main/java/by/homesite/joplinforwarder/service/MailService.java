package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.MailRepository;
import by.homesite.joplinforwarder.service.mapper.MailMessageMapper;
import by.homesite.joplinforwarder.util.ImapUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;

import com.sun.mail.imap.IMAPMessage;

@Service
public class MailService {
    private final SettingsService settingsService;
    private final RulesService rulesService;
    private final MailRepository mailRepository;
    
    private final MailMessageMapper mailMessageMapper;

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public MailService(SettingsService settingsService, RulesService rulesService, MailRepository mailRepository,
            MailMessageMapper mailMessageMapper) {
        this.settingsService = settingsService;
        this.rulesService = rulesService;
        this.mailRepository = mailRepository;
        this.mailMessageMapper = mailMessageMapper;
    }

    public void getMail() {
        List<User> users = this.settingsService.getMailSettingsByUsers();
        users.forEach(user -> {
            String messageId = getLastSavedMessageId(user.getId());
            fetchAndSaveMails(user, messageId);
        });
    }

    private void fetchAndSaveMails(User user, String lastSavedMessageId) {
        String mailServer = settingsService.getSettingValue(user.getSettingsList(), "mailserver");
        String mailPort = settingsService.getSettingValue(user.getSettingsList(), "mailport");
        String mailUser = settingsService.getSettingValue(user.getSettingsList(), "username");
        String mailPassword = settingsService.getSettingValue(user.getSettingsList(), "password");

        if (!StringUtils.hasText(mailServer) || !StringUtils.hasText(mailPort)) {
            return;
        }

        Properties properties = ImapUtil.configureImapConnection();

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
                String id = ((IMAPMessage) mess).getMessageID();
                if (!StringUtils.hasText(id) || getMailByMessageId(user, id) != null) {
                    continue;
                }
                if (id.equals(lastSavedMessageId)) {
                    break;
                }
                
                Mail mail = mailMessageMapper.toDto((IMAPMessage) mess);

                mail.setUser(user);
                mail.setRule(rulesService.getUserRule(mail, user));

                if (mail.getRule() != null)
                {
                    mailRepository.save(mail);
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

    public void deleteOldItems(int purgeMailsPeriod) {
    }

}

package by.homesite.joplinforwarder.service.parser;

import org.eclipse.angus.mail.imap.IMAPMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.RuleAction;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.MailRepository;
import by.homesite.joplinforwarder.service.RulesService;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.parser.mapper.IMAPMailMessageMapper;
import by.homesite.joplinforwarder.service.storage.StorageService;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Flags;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;

import static by.homesite.joplinforwarder.util.GlobUtil.settingValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class IMAPParserServiceTests {

    @Mock
    private MailRepository mailRepository;
    
    @Mock
    private SettingsService settingsService;
    
    @Mock
    private RulesService rulesService;
    
    @Mock
    private IMAPMailMessageMapper imapMailMessageMapper;
    
    @Mock
    private StorageService storageService;

    @InjectMocks
    private IMAPParserService imapParserService;

    @Test
    public void testParseMail_ProcessFinalSteps() throws MessagingException {
        // Arrange
        User user = new User(); // Create a user for testing
        user.setId(12);
        List<Settings> settingsList = Collections.singletonList(new Settings());
        user.setSettings(settingsList);
        Mail mail = new Mail(); // Create a mail for testing
        mail.setMessageId("testMsgId");
        mail.setId(123);
        mail.setRule(new Rule()); // Set a rule for the mail
        mail.getRule().setLast_processed_at(OffsetDateTime.now().minusDays(1)); // Set last processed time to 1 day ago
        mail.getRule().setProcessed(5); // Set processed count to 5
        RuleAction ruleAction = new RuleAction();
        ruleAction.setAction("MARK_READ");
        List<RuleAction> actions = List.of(ruleAction);
        mail.getRule().setRuleActions(actions);

        IMAPMessage mockMessage = mock(IMAPMessage.class);
        Store mockStore = mock(Store.class);
        Folder folderObject = mock(Folder.class);
        jakarta.mail.Session sessionManager = mock(jakarta.mail.Session.class);

        // Mock 
        when(settingsService.getMailSettingsByUsers()).thenReturn(Collections.singletonList(user));
        when(settingValue(eq(user), anyString())).thenReturn(String.valueOf((int) (Math.random() * 1000)));
        when(mailRepository.findTop1ByUserIdOrderByReceivedDesc(12)).thenReturn(mail);
        try (MockedStatic<Session> session = Mockito.mockStatic(jakarta.mail.Session.class)) {
            session.when(() -> jakarta.mail.Session.getInstance(any())).thenReturn(sessionManager);
            when(sessionManager.getStore("imaps")).thenReturn(mockStore);
            when(mockStore.getFolder("INBOX")).thenReturn(folderObject);
            IMAPMessage imapMessage = mock(IMAPMessage.class);
            Message[] mockMessages = new Message[1];
            mockMessages[0] = imapMessage;
            when(folderObject.getMessages()).thenReturn(mockMessages);
            when(imapMessage.getMessageID()).thenReturn("tets");
            Flags flags = new Flags();
            when(imapMessage.getFlags()).thenReturn(flags);
            when(imapMailMessageMapper.toDto((IMAPMessage) any())).thenReturn(mail);
            when(rulesService.getUserRule(any(), any())).thenReturn(mail.getRule());
            when(mailRepository.save(mail)).thenReturn(mail);
            when(mailRepository.getReferenceById(123L)).thenReturn(mail);

            // Act
            imapParserService.parseMail();

            // Assert
            verify(mailRepository, times(3)).save(mail); // Verify that the mail was saved
            assertEquals(6, mail.getRule().getProcessed()); // Assert that the processed count was incremented
        }

    }
}
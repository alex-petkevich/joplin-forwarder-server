package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class WebDAVStorageServiceTests {

    @Mock
    SettingsService settingsService;

    @InjectMocks
    WebDAVStorageService webDAVStorageService;

    @Test
    void testStoreRecordGetList() {
        User user = new User();
        user.setId(1);
        List<Settings> settingsList = Collections.emptyList();
        user.setSettingsList(settingsList);

        when(settingsService.getSettingValue(settingsList, "joplinserverdavurl")).thenReturn("https://dav.abcdef.at/");
        when(settingsService.getSettingValue(settingsList, "joplinserverdavusername")).thenReturn("davuser");
        when(settingsService.getSettingValue(settingsList, "joplinserverdavpassword")).thenReturn("!Qazxsw234");

        Mail mail = new Mail();
        mail.setSubject("testRecord");
        mail.setText("test test test!!!");

        webDAVStorageService.storeRecord(user, mail);

    }
}

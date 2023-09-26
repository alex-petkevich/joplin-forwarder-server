package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Qualifier("storageServiceStrategy")
@Service
public class StorageServiceImpl implements StorageService {

    private final SettingsService               settingsService;
    private final WebDAVStorageService          webDAVStorageService;
    private final JoplinServerStorageService    joplinServerStorageService;
    private final LocalStorageService           localStorageService;

    public StorageServiceImpl(SettingsService settingsService,
                              WebDAVStorageService webDAVStorageService,
                              JoplinServerStorageService joplinServerStorageService,
                              LocalStorageService localStorageService) {
        this.settingsService = settingsService;
        this.webDAVStorageService = webDAVStorageService;
        this.joplinServerStorageService = joplinServerStorageService;
        this.localStorageService = localStorageService;
    }

    @Override
    public void storeRecord(User user, Mail mail) {
        String joplinserver = settingsService.getSettingValue(user.getSettingsList(), "joplinserver");

        if (mail.getRule() == null || !StringUtils.hasText(joplinserver)) {
            return;
        }

        switch (joplinserver) {
            case "WEBDAV" -> webDAVStorageService.storeRecord(user, mail);
            case "SERVER" -> joplinServerStorageService.storeRecord(user, mail);
            case "LOCAL" -> localStorageService.storeRecord(user, mail);
            default -> {
            }
        }

    }
}

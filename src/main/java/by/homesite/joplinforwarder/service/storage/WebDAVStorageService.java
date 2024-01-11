package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.dto.JoplinAttachment;
import by.homesite.joplinforwarder.service.dto.JoplinItem;
import by.homesite.joplinforwarder.service.dto.JoplinNode;
import by.homesite.joplinforwarder.service.storage.client.DavClient;
import by.homesite.joplinforwarder.service.storage.client.dto.DavFile;
import by.homesite.joplinforwarder.service.storage.client.dto.DavFileInputStream;
import by.homesite.joplinforwarder.service.storage.client.dto.DavList;
import by.homesite.joplinforwarder.service.storage.mapper.JoplinItemMailMapper;
import by.homesite.joplinforwarder.util.JoplinParserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.jackrabbit.webdav.DavException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class WebDAVStorageService extends AbstractStorageService {

    public static final String FILE_LOCKS_EXT = ".json";
    public static final String APP_ID = "a5fe93768f344188b162a55319bf753e";
    public static final String WEB_DAV_PROCESSING_ERROR = "WebDAV processing error";
    private static final int RETRY_ATTEMPTS = 10;
    public static final String LOCKS_FOLDER = "locks/";

    private final DavClient davClient = new DavClient();

    private static final Logger log = LoggerFactory.getLogger(WebDAVStorageService.class);

    protected WebDAVStorageService(SettingsService settingsService, JoplinItemMailMapper mailMapper, JoplinParserUtil joplinParserUtil, ObjectMapper objectMapper) {
        super(settingsService, mailMapper, joplinParserUtil, objectMapper);
    }

    @Override
    public String storeRecord(User user, Mail mail) {

        return super.storeRecord(user, mail);
    }

    protected void storeJoplinNodeInStorage(User user, byte[] fileContent, String fileName) {

        try
        {
            int pauseCycles = 0;
            while(this.hasLock(user, "2") && pauseCycles < RETRY_ATTEMPTS) {
                TimeUnit.SECONDS.sleep(1);
                pauseCycles++;
            }
            if (pauseCycles == RETRY_ATTEMPTS) {
                log.error("WebDAV Joplin: Can not get write lock to the database");
                return;
            }
            this.createLock(user, "2");

            getDavClient(user).put(fileContent, fileName);
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        this.releaseLock(user, "2");
    }

    private void createLock(User user, String type) {
        String fileContent = """
{"type":%s,"clientType":1,"clientId":"%s"}
                """.formatted(type, APP_ID);
        try {
            getDavClient(user).put(fileContent.getBytes(), LOCKS_FOLDER + type + "_1_" + APP_ID + FILE_LOCKS_EXT);
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }
    }

    private void releaseLock(User user, String type) {
        try {
           if (!getDavClient(user).delete(LOCKS_FOLDER + type + "_1_" + APP_ID + FILE_LOCKS_EXT)) {
               log.warn("Not able to release lock - it does not exists");
           }
        } catch (DavClient.DavAccessFailedException | IOException e) {
            log.warn("Not able to release lock - it does not exists");
        }
    }

    private boolean hasLock(User user, String lockType) {

        try
        {
            DavList resources = getDavClient(user).list(LOCKS_FOLDER);

            List<DavFile> files = resources.getFiles();
            for (DavFile item : files) {
                if (item.getName().endsWith(FILE_LOCKS_EXT)) {
                    String lockName = item.getName().replace(FILE_LOCKS_EXT, "");
                    String[] lockDesc = lockName.split("_");
                    if (lockDesc.length > 2
                            && (lockType.equals(lockDesc[0]) && !APP_ID.equals(lockDesc[2]))) {
                            return true;
                    }
                }
            }

            return false;
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }

        return false;
    }

    private DavClient getDavClient(User user) {

        if (davClient.isReady()) {
            return davClient;
        }

        String joplinserverdavurl = settingsService.getSettingValue(user.getSettingsList(), "joplinserverdavurl");
        String joplinserverdavusername = settingsService.getSettingValue(user.getSettingsList(), "joplinserverdavusername");
        String joplinserverdavpassword = settingsService.getSettingValue(user.getSettingsList(), "joplinserverdavpassword");

        if (StringUtils.hasText(joplinserverdavurl) && StringUtils.hasText(joplinserverdavusername) && StringUtils.hasText(joplinserverdavpassword)) {
            URI url = URI.create(joplinserverdavurl);
            davClient.init(url, joplinserverdavusername, joplinserverdavpassword);
        }

        return davClient;
    }

    protected List<JoplinItem> getDBItemsList(User user) {

        List<JoplinItem> result = new ArrayList<>();

        try
        {
            DavList resources = getDavClient(user).list("");

            List<DavFile> files = resources.getFiles();
            for (DavFile item : files) {
                if (item.getName().endsWith(FILE_EXT)) {
                    try (DavFileInputStream itemContent = davClient.readFile(item)) {
                        result.add(joplinParserUtil.textToNode(item.getName(), new String(itemContent.readAllBytes())));
                    }
                }
            }

            return cacheNodesList(user, result);
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }


        return cacheNodesList(user, Collections.emptyList());
    }

}

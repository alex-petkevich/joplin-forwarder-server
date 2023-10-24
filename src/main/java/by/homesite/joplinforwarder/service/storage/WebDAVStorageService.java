package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.dto.JoplinItem;
import by.homesite.joplinforwarder.service.dto.JoplinNode;
import by.homesite.joplinforwarder.service.storage.client.DavClient;
import by.homesite.joplinforwarder.service.storage.client.dto.DavFile;
import by.homesite.joplinforwarder.service.storage.client.dto.DavFileInputStream;
import by.homesite.joplinforwarder.service.storage.client.dto.DavList;
import by.homesite.joplinforwarder.service.storage.mapper.JoplinItemMailMapper;
import by.homesite.joplinforwarder.util.JoplinParserUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebDAVStorageService implements StorageService {

    public static final String FILE_EXT = ".md";

    private final SettingsService settingsService;
    private final JoplinItemMailMapper mailMapper;
    private final JoplinParserUtil joplinParserUtil;
    private final DavClient davClient = new DavClient();

    private static final Logger log = LoggerFactory.getLogger(WebDAVStorageService.class);

    public WebDAVStorageService(SettingsService settingsService, JoplinItemMailMapper mailMapper, JoplinParserUtil joplinParserUtil) {
        this.settingsService = settingsService;
        this.mailMapper = mailMapper;
        this.joplinParserUtil = joplinParserUtil;
    }

    @Override
    public String storeRecord(User user, Mail mail) {

        String parentId = "";
        if (mail.getRule() != null && StringUtils.hasText(mail.getRule().getSave_in_parent_id())) {
            List<JoplinItem> items = getDBItemsList(user);
            Optional<JoplinItem> parentNode = items.stream().filter(it ->
                    it.getType_() == JoplinParserUtil.TYPE_NODE && mail.getRule().getSave_in_parent_id().equals(it.getId())
            ).findFirst();
            if (parentNode.isEmpty()) {
                parentId = createJoplinNode(user, mail.getRule().getSave_in_parent_id());
            } else {
                parentId = parentNode.get().getId();
            }
        }

        return storeNode(user, mail, parentId);
    }

    private String createJoplinNode(User user, String saveInParentId) {
        JoplinNode joplinNode = new JoplinNode();

        joplinNode.setId(UUID.randomUUID().toString().replace("-", ""));
        joplinNode.setName(saveInParentId);
        joplinNode.setCreatedTime(LocalDateTime.now());
        joplinNode.setUpdatedTime(LocalDateTime.now());
        joplinNode.setUserCreatedTime(LocalDateTime.now());
        joplinNode.setUserUpdatedTime(LocalDateTime.now());
        joplinNode.setType_(2);

        String fileName = joplinNode.getId() + FILE_EXT;
        String fileContent = joplinParserUtil.nodeToText(joplinNode);

        try
        {
            getDavClient(user).put(fileContent.getBytes(), fileName);
        } catch (DavException | IOException e) {
            log.error("WebDAV processing error");
        }

        return joplinNode.getId();
    }

    private String storeNode(User user, Mail mail, String parentId) {
        JoplinItem jNode = mailMapper.toDto(mail);

        if (!StringUtils.hasText(jNode.getId())) {
            return "";
        }

        jNode.setParentId(parentId);

        String fileName = jNode.getId() + FILE_EXT;
        String fileContent = joplinParserUtil.itemToText(jNode);

        try
        {
            getDavClient(user).put(fileContent.getBytes(), fileName);
        } catch (DavException | IOException e) {
            log.error("WebDAV processing error");
        }
        return jNode.getId();
    }

    private List<JoplinItem> getDBItemsList(User user) {

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

            return result;
        } catch (DavException | IOException e) {
            log.error("WebDAV processing error");
        }


        return Collections.emptyList();
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

}

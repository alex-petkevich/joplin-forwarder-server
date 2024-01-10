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

@Data
@Component
public class WebDAVStorageService implements StorageService {

    public static final String FILE_EXT = ".md";
    public static final String FILE_LOCKS_EXT = ".json";
    public static final String APP_ID = "a5fe93768f344188b162a55319bf753e";
    public static final String WEB_DAV_PROCESSING_ERROR = "WebDAV processing error";
    private static final int RETRY_ATTEMPTS = 10;
    public static final String LOCKS_FOLDER = "locks/";

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

        if (mail.getRule() == null) {
            return "";
        }

        return Boolean.TRUE.equals(mail.getRule().getSave_in())
                ? storeNewItem(user, mail, mail.getRule().getSave_in_parent_id())
                : storeExistingItem(user, mail, mail.getRule().getSave_in_parent_id());
    }

    private String storeExistingItem(User user, Mail mail, String saveInParentId) {
        JoplinItem parentItem = null;

        if (!StringUtils.hasText(saveInParentId)) {
            return "";
        } else {
            String rootParentNodeId = getParentNodeId(user, mail, "");
            
            List<JoplinItem> items = getDBItemsList(user);
            Optional<JoplinItem> item = items.stream().filter(it ->
                    it.getType_() == JoplinParserUtil.TYPE_ITEM && saveInParentId.trim().equalsIgnoreCase(it.getId().trim())
            ).findFirst();
            parentItem = item.orElseGet(() -> mailMapper.toDto(mail));
            if (!rootParentNodeId.isEmpty()) {
                parentItem.setParentId(rootParentNodeId);
            }
            
        }

        String content = parentItem.getContent() + "\n\n #" + mail.getSubject() + " " + mail.getReceived() + addAttachmentsToContent(user, mail.getAttachments());
        parentItem.setContent(content);

        String fileName = parentItem.getId() + FILE_EXT;
        String fileContent = joplinParserUtil.itemToText(parentItem);

        storeJoplinNodeInStorage(user, fileContent.getBytes(), fileName);

        return parentItem.getId();
    }

    private void storeJoplinNodeInStorage(User user, byte[] fileContent, String fileName) {

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

    private String createJoplinNode(User user, String saveInParentId) {
        JoplinNode joplinNode = new JoplinNode();

        joplinNode.setId(generateFileName());
        joplinNode.setName(saveInParentId);
        joplinNode.setCreatedTime(LocalDateTime.now());
        joplinNode.setUpdatedTime(LocalDateTime.now());
        joplinNode.setUserCreatedTime(LocalDateTime.now());
        joplinNode.setUserUpdatedTime(LocalDateTime.now());
        joplinNode.setType_(JoplinParserUtil.TYPE_NODE);

        String fileName = joplinNode.getId() + FILE_EXT;
        String fileContent = joplinParserUtil.nodeToText(joplinNode);

        storeJoplinNodeInStorage(user, fileContent.getBytes(), fileName);

        return joplinNode.getId();
    }

    private String storeJoplinAttachment(User user, String attachName, int num) {

        Path realFile = Paths.get(attachName);
        if (!Files.exists(realFile)) {
            return "";
        }

        JoplinAttachment attach = new JoplinAttachment();

        attach.setFileExtension(StringUtils.getFilenameExtension(attachName));
        String attachFileName = num + "_" + generateFileName() + "." + attach.getFileExtension();
        attach.setId(generateFileName());
        attach.setName(attachFileName);
        attach.setCreatedTime(LocalDateTime.now());
        attach.setUpdatedTime(LocalDateTime.now());
        attach.setUserCreatedTime(LocalDateTime.now());
        attach.setUserUpdatedTime(LocalDateTime.now());
        attach.setType_(JoplinParserUtil.TYPE_RESOURCE);
        attach.setFilename(attachName);
        try {
            attach.setSize(Files.size(realFile));
            attach.setMime(Files.probeContentType(realFile));
        }
        catch (IOException e) {
            log.error("Attachment not found");
        }

        String fileName = attach.getId() + FILE_EXT;
        String fileContent = joplinParserUtil.attachToText(attach);

        try
        {
            storeJoplinNodeInStorage(user, fileContent.getBytes(), fileName);
            storeJoplinNodeInStorage(user, Files.readAllBytes(realFile), ".resource/" + attach.getId());
        } catch (IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }
        String image = attach.getMime() != null && attach.getMime().split("/")[0].equals("image") ? "!" : "";

        return "%s[%s](:/%s)".formatted(image, realFile.getFileName(), attach.getId());
    }

    private String generateFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String storeNewItem(User user, Mail mail, String parentId) {

        if (mail.getRule() != null && StringUtils.hasText(parentId)) {
            parentId = getParentNodeId(user, mail, mail.getRule().getSave_in_parent_id());
        }

        JoplinItem jNode = mailMapper.toDto(mail);

        if (!StringUtils.hasText(jNode.getId())) {
            return "";
        }

        jNode.setParentId(parentId);
        jNode.setContent(jNode.getContent() + addAttachmentsToContent(user, mail.getAttachments()));

        String fileName = jNode.getId() + FILE_EXT;
        String fileContent = joplinParserUtil.itemToText(jNode);

        storeJoplinNodeInStorage(user, fileContent.getBytes(), fileName);
        return jNode.getId();
    }

    private String addAttachmentsToContent(User user, String attachments) {
        StringBuilder result = new StringBuilder("\n\n");
        String[] atts = attachments.split("\\|");
        for (int i=0; i < atts.length; i++) {
            result.append(storeJoplinAttachment(user, atts[i], i)).append("\n");
        }
        return result.toString();
    }

    private String getParentNodeId(User user, Mail mail, String parentNodeId)
    {
        List<JoplinItem> items = getDBItemsList(user);

        String settingsParentId = "";
        String settingsNode = settingsService.getSettingValue(user.getSettingsList(), "joplinserverparentnode");
        if (StringUtils.hasText(settingsNode)) {
            Optional<JoplinItem> settingsParentNode = items.stream().filter(it ->
                    it.getType_() == JoplinParserUtil.TYPE_NODE && settingsNode.trim().equalsIgnoreCase(it.getContent().trim())
            ).findFirst();
            
            if (settingsParentNode.isEmpty()) {
                settingsParentId = createJoplinNode(user, settingsParentId);
            } else {
                settingsParentId = settingsParentNode.get().getId();
            }
        }
        
        if (!StringUtils.hasText(parentNodeId)) {
            return settingsParentId;
        }
        
        Optional<JoplinItem> parentNode = items.stream().filter(it ->
                it.getType_() == JoplinParserUtil.TYPE_NODE && parentNodeId.trim().equalsIgnoreCase(it.getContent().trim())
        ).findFirst();
        if (parentNode.isEmpty()) {
            return createJoplinNode(user, settingsParentId);
        } else {
            return parentNode.get().getId();
        }
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
            log.error(WEB_DAV_PROCESSING_ERROR);
        }


        return Collections.emptyList();
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

}

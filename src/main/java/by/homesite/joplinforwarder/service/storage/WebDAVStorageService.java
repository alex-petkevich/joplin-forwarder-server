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
import java.io.File;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Component
public class WebDAVStorageService implements StorageService {

    public static final String FILE_EXT = ".md";
    public static final String WEB_DAV_PROCESSING_ERROR = "WebDAV processing error";

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

        return  mail.getRule().getSave_in() == 1
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

        try
        {
            getDavClient(user).put(fileContent.getBytes(), fileName);
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }

        return parentItem.getId();
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

        try
        {
            getDavClient(user).put(fileContent.getBytes(), fileName);
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }

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
        attach.setType_(JoplinParserUtil.TYPE_FILE);
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
            getDavClient(user).put(fileContent.getBytes(), fileName);
            getDavClient(user).put(Files.readAllBytes(realFile), ".resource/" + attach.getId());
        } catch (DavException | IOException e) {
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

        try
        {
            getDavClient(user).put(fileContent.getBytes(), fileName);
        } catch (DavException | IOException e) {
            log.error(WEB_DAV_PROCESSING_ERROR);
        }
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

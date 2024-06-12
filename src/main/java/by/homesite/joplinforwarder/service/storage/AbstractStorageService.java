package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.dto.JoplinAttachment;
import by.homesite.joplinforwarder.service.dto.JoplinItem;
import by.homesite.joplinforwarder.service.dto.JoplinNode;
import by.homesite.joplinforwarder.service.storage.mapper.JoplinItemMailMapper;
import by.homesite.joplinforwarder.util.JoplinParserUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static by.homesite.joplinforwarder.util.GlobUtil.settingValue;
import static java.util.Map.entry;

public abstract class AbstractStorageService implements StorageService {

    public static final String FILE_EXT = ".md";

    private static final Logger log = LoggerFactory.getLogger(AbstractStorageService.class);

    protected final JoplinItemMailMapper mailMapper;
    protected final SettingsService settingsService;
    protected final JoplinParserUtil joplinParserUtil;
    protected final ObjectMapper objectMapper;

    protected AbstractStorageService(SettingsService settingsService, JoplinItemMailMapper mailMapper, JoplinParserUtil joplinParserUtil, ObjectMapper objectMapper) {
        this.settingsService = settingsService;
        this.mailMapper = mailMapper;
        this.joplinParserUtil = joplinParserUtil;
        this.objectMapper = objectMapper;
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

    protected String storeExistingItem(User user, Mail mail, String saveInParentId) {
        JoplinItem parentItem = null;

        if (!StringUtils.hasText(saveInParentId)) {
            return "";
        } else {
            String rootParentNodeId = getParentNodeId(user, "");

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

    /**
     * Abstract method, must be implemented in inherited class
     *
     * @param user
     * @param fileContent
     * @param fileName
     */
    protected abstract void storeJoplinNodeInStorage(User user, byte[] fileContent, String fileName);

    protected String createJoplinNode(User user, String saveInParentId) {
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

    protected String storeJoplinAttachment(User user, String attachName, int num) {

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
            log.error("Resouce saving error");
        }
        String image = attach.getMime() != null && attach.getMime().split("/")[0].equals("image") ? "!" : "";

        return "%s[%s](:/%s)".formatted(image, realFile.getFileName(), attach.getId());
    }


    protected String generateFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    protected String storeNewItem(User user, Mail mail, String parentId) {

        if (mail.getRule() != null && StringUtils.hasText(parentId)) {
            parentId = getParentNodeId(user, mail.getRule().getSave_in_parent_id());
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

    private String getParentNodeId(User user, String parentNodeId)
    {
        List<JoplinItem> items = getDBItemsList(user);

        String settingsParentId = "";
        String settingsNode = settingValue(user, "joplinserverparentnode");
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

    protected abstract List<JoplinItem> getDBItemsList(User user);

    protected List<JoplinItem> cacheNodesList(User user, List<JoplinItem> result) {
        List<JoplinItem> parentNodes = result.stream().filter(it -> it.getType_() == JoplinParserUtil.TYPE_NODE).toList();
        List<Map<String, String>> cache = new ArrayList<>();

        for (JoplinItem item: parentNodes) {
            cache.add(Map.ofEntries(
                    entry("id", item.getId()),
                    entry("parentId", item.getParentId() != null ? item.getParentId() : ""),
                    entry("name", item.getContent())
            ));
        }

        try {
            this.settingsService.setSettingValue(user, "joplinnodescachedlist", objectMapper.writeValueAsString(cache));
        } catch (JsonProcessingException e) {
            log.error("Can not serialize nodes list for cache");
        }

        return result;
    }

}

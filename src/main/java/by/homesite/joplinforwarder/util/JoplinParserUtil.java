package by.homesite.joplinforwarder.util;

import by.homesite.joplinforwarder.service.dto.JoplinAttachment;
import by.homesite.joplinforwarder.service.dto.JoplinItem;
import by.homesite.joplinforwarder.service.dto.JoplinNode;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JoplinParserUtil {

    private final SpringTemplateEngine templateEngine;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_NODE = 2;
    public static final int TYPE_SETTING = 3;
    public static final int TYPE_RESOURCE = 4;
    public static final int TYPE_TAG = 5;
    public static final int TYPE_NODETAG = 6;
    public static final int TYPE_SEARCH = 7;
    public static final int TYPE_ALARM = 8;
    public static final int TYPE_MASTERKEY = 9;
    public static final int TYPE_ITEMCHANGE = 10;
    public static final int TYPE_NOTERESOURCE = 11;
    public static final int TYPE_RESOURCELOCALSTATE = 12;
    public static final int TYPE_REVISION = 13;
    public static final int TYPE_SMARTFILTER = 15;
    public static final int TYPE_COMMAND = 16;

    public JoplinParserUtil(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public JoplinItem textToNode(String fileName, String inputText) {

        JoplinItem jNode = new JoplinItem();
        String[] fileStrings = inputText.split("\n");
        boolean isContent = true;
        StringBuilder content = new StringBuilder();
        Map<String, String> properties = new HashMap<>();
        for(String str: fileStrings) {
            String[] current = str.split(": ");
            if (str.contains(": ")) {
                String key = current[0].trim();
                String val = current.length > 1 ? current[1].trim() : "";
                properties.put(key, val);
                isContent = false;
            }
            if (isContent) {
                content.append(str);
            }
        }

        jNode.setName(fileName);
        jNode.setId(properties.get("id"));
        jNode.setType_(getInt(properties.get("type_")));
        jNode.setParentId(properties.get("parent_id"));
        jNode.setContent(String.valueOf(content).trim());
        jNode.setUpdatedTime(getTime(properties.get("updated_time")));
        jNode.setCreatedTime(getTime(properties.get("created_time")));

        return jNode;
    }

    private LocalDateTime getTime(String time) {
        try {
            return LocalDateTime.parse(time);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    private int getInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    public String nodeToText(JoplinNode inputText) {

        Context context = new Context();
        context.setVariable("node", inputText);

        return templateEngine.process("joplin/node", context);
    }

    public String itemToText(JoplinItem inputText) {

        Context context = new Context();
        context.setVariable("node", inputText);

        return templateEngine.process("joplin/item", context);
    }

    public String attachToText(JoplinAttachment inputText) {

        Context context = new Context();
        context.setVariable("node", inputText);

        return templateEngine.process("joplin/attach", context);
    }
}

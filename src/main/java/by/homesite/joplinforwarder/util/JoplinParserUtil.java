package by.homesite.joplinforwarder.util;

import by.homesite.joplinforwarder.service.dto.JoplinItem;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.List;
import java.util.Optional;

@Component
public class JoplinParserUtil {

    private final SpringTemplateEngine templateEngine;

    public JoplinParserUtil(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public JoplinItem textToNode(String inputText) {

        JoplinItem jNode = new JoplinItem();


        return jNode;
    }

    public String nodeToText(JoplinItem inputText) {

        Context context = new Context();
        context.setVariable("node", inputText);

        return templateEngine.process("joplin/item", context);
    }

    public String getIdByName(List<JoplinItem> dbItemsList, String name) {
        Optional<JoplinItem> node = dbItemsList.stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst();
        return node.isPresent() ? node.get().getId() : "";
    }
}

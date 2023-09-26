package by.homesite.joplinforwarder.util;

import by.homesite.joplinforwarder.service.dto.JoplinNode;
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

    public JoplinNode textToNode(String inputText) {

        JoplinNode jNode = new JoplinNode();


        return jNode;
    }

    public String nodeToText(JoplinNode inputText) {

        Context context = new Context();
        context.setVariable("node", inputText);

        return templateEngine.process("joplin/node", context);
    }

    public String getIdByName(List<JoplinNode> dbItemsList, String name) {
        Optional<JoplinNode> node = dbItemsList.stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst();
        return node.isPresent() ? node.get().getId() : "";
    }
}

package by.homesite.joplinforwarder.service.storage.mapper;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.service.dto.JoplinNode;
import by.homesite.joplinforwarder.util.BasicMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class JoplinNodeMailMapper implements BasicMapper<JoplinNode, Mail> {
    
    @Override
    public Mail toEntity(JoplinNode dto) {
        return null;
    }

    @Override
    public JoplinNode toDto(Mail entity) {
        JoplinNode jNode = new JoplinNode();

        jNode.setId(UUID.randomUUID().toString());
        jNode.setContent(entity.getText());
        jNode.setName(entity.getSubject());
        jNode.setCreatedTime(LocalDateTime.now());
        jNode.setUpdatedTime(LocalDateTime.now());
        jNode.setIsConflict(0);
        jNode.setLatitude("0.00000000");
        jNode.setLongitude("0.00000000");
        jNode.setAltitude("0.0000");
        jNode.setIsTodo(0);
        jNode.setTodoDue(0);
        jNode.setTodoCompleted(0);
        jNode.setSource("joplin-desktop");
        jNode.setSourceApplication("net.cozic.joplin-desktop");
        jNode.setOrder(System.currentTimeMillis() / 1000);
        jNode.setUserCreatedTime(LocalDateTime.now());
        jNode.setUserUpdatedTime(LocalDateTime.now());
        jNode.setEncryptionApplied(0);
        jNode.setMarkupLanguage(1);
        jNode.setIsShared(0);
        jNode.setType_(1);

        return jNode;
    }

    @Override
    public List<Mail> toEntity(List<JoplinNode> dtoList) {
        return null;
    }

    @Override
    public List<JoplinNode> toDto(List<Mail> entityList) {
        return null;
    }
}

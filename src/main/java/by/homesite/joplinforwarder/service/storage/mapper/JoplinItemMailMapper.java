package by.homesite.joplinforwarder.service.storage.mapper;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.service.dto.JoplinItem;
import by.homesite.joplinforwarder.util.BasicMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class JoplinItemMailMapper implements BasicMapper<JoplinItem, Mail> {

    @Override
    public Mail toEntity(JoplinItem dto) {
        return null;
    }

    @Override
    public JoplinItem toDto(Mail entity) {
        JoplinItem joplinItem = new JoplinItem();

        joplinItem.setId(UUID.randomUUID().toString().replace("-", ""));
        joplinItem.setContent(entity.getText());
        joplinItem.setName(entity.getSubject());
        joplinItem.setCreatedTime(LocalDateTime.now());
        joplinItem.setUpdatedTime(LocalDateTime.now());
        joplinItem.setIsConflict(0);
        joplinItem.setLatitude("0.00000000");
        joplinItem.setLongitude("0.00000000");
        joplinItem.setAltitude("0.0000");
        joplinItem.setIsTodo(0);
        joplinItem.setTodoDue(0);
        joplinItem.setTodoCompleted(0);
        joplinItem.setSource("joplin-desktop");
        joplinItem.setSourceApplication("net.cozic.joplin-desktop");
        joplinItem.setOrder(System.currentTimeMillis() / 1000);
        joplinItem.setUserCreatedTime(LocalDateTime.now());
        joplinItem.setUserUpdatedTime(LocalDateTime.now());
        joplinItem.setEncryptionApplied(0);
        joplinItem.setMarkupLanguage(1);
        joplinItem.setIsShared(0);
        joplinItem.setType_(1);

        return joplinItem;
    }

    @Override
    public List<Mail> toEntity(List<JoplinItem> dtoList) {
        return null;
    }

    @Override
    public List<JoplinItem> toDto(List<Mail> entityList) {
        return null;
    }
}

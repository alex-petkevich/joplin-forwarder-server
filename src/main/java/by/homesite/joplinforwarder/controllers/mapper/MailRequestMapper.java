package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.request.MailRequest;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.util.BasicMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { UserMapper.class})
public interface MailRequestMapper extends BasicMapper<Mail, MailRequest>
{

    MailRequest toEntity(Mail mail);

    Mail toDto(Mail mail);

}

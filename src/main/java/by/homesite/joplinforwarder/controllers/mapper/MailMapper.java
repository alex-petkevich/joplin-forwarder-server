package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.response.MailResponse;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.util.BasicMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { UserMapper.class, RuleMapper.class})
public interface MailMapper extends BasicMapper<Mail, MailResponse>
{

    @Mapping(source = "user.id", target = "user_id")
    @Mapping(source = "rule.name", target = "rule_name")
    @Mapping(source = "rule.id", target = "rule_id")
    @Mapping(source = "attachments", target = "attachList", qualifiedByName = "attach")
    MailResponse toEntity(Mail mail);

    @Named("attach")
    public static String[] attachParser(String attach) {
        if (!StringUtils.hasText(attach)) {
            return new String[]{};
        }

        return Arrays.stream(attach.split("\\|")).map(it -> Path.of(it).getFileName().toString()).toList().toArray(new String[0]);
    }
}

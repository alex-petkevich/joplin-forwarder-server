package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.response.RuleResponse;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.util.BasicMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class})
public interface RuleMapper extends BasicMapper<Rule, RuleResponse>
{

    @Mapping(source = "user.id", target = "user_id")
    RuleResponse toEntity(Rule rule);

}

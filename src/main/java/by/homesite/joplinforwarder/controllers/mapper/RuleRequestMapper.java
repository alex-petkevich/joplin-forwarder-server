package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.request.RuleRequest;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.util.BasicMapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { UserMapper.class})
public interface RuleRequestMapper extends BasicMapper<Rule, RuleRequest>
{

    RuleRequest toEntity(Rule rule);

    Rule toDto(RuleRequest rule);

}

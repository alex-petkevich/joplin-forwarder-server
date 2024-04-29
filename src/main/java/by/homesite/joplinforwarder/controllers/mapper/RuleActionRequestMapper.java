package by.homesite.joplinforwarder.controllers.mapper;

import org.mapstruct.Mapper;

import by.homesite.joplinforwarder.controllers.dto.request.RuleActionRequest;
import by.homesite.joplinforwarder.controllers.dto.request.RuleRequest;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.RuleAction;
import by.homesite.joplinforwarder.util.BasicMapper;

@Mapper(componentModel = "spring")
public interface RuleActionRequestMapper extends BasicMapper<RuleAction, RuleActionRequest>
{

    RuleActionRequest toEntity(RuleAction rule);

    RuleAction toDto(RuleActionRequest rule);

}

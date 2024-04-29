package by.homesite.joplinforwarder.controllers.mapper;

import org.mapstruct.Mapper;

import by.homesite.joplinforwarder.controllers.dto.request.RuleConditionRequest;
import by.homesite.joplinforwarder.model.RuleCondition;
import by.homesite.joplinforwarder.util.BasicMapper;

@Mapper(componentModel = "spring")
public interface RuleConditionRequestMapper extends BasicMapper<RuleCondition, RuleConditionRequest>
{

    RuleConditionRequest toEntity(RuleCondition rule);

    RuleCondition toDto(RuleConditionRequest rule);

}

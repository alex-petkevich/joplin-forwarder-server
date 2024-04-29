package by.homesite.joplinforwarder.controllers;

import by.homesite.joplinforwarder.controllers.dto.request.RuleActionRequest;
import by.homesite.joplinforwarder.controllers.dto.request.RuleConditionRequest;
import by.homesite.joplinforwarder.controllers.dto.request.RuleRequest;
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.controllers.dto.response.RuleResponse;
import by.homesite.joplinforwarder.controllers.mapper.RuleActionRequestMapper;
import by.homesite.joplinforwarder.controllers.mapper.RuleConditionRequestMapper;
import by.homesite.joplinforwarder.controllers.mapper.RuleMapper;
import by.homesite.joplinforwarder.controllers.mapper.RuleRequestMapper;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.RuleAction;
import by.homesite.joplinforwarder.model.RuleCondition;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.RulesService;
import by.homesite.joplinforwarder.service.TranslateService;
import by.homesite.joplinforwarder.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rules")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class RulesController
{
	final
	UserService userService;

	final
	RulesService rulesService;

	final
	RuleMapper ruleMapper;

	final
	RuleRequestMapper ruleRequestMapper;

    final
    RuleActionRequestMapper ruleActionRequestMapper;

    final
    RuleConditionRequestMapper ruleConditionRequestMapper;
          
    final
    TranslateService translate;

    public RulesController(UserService userService, RulesService rulesService, RuleMapper ruleMapper, RuleRequestMapper ruleRequestMapper, RuleActionRequestMapper ruleActionRequestMapper, RuleConditionRequestMapper ruleConditionRequestMapper, TranslateService translate) {
		this.userService = userService;
		this.rulesService = rulesService;
		this.ruleMapper = ruleMapper;
		this.ruleRequestMapper = ruleRequestMapper;
        this.ruleActionRequestMapper = ruleActionRequestMapper;
        this.ruleConditionRequestMapper = ruleConditionRequestMapper;
        this.translate = translate;
    }

	@GetMapping("/")
	@ResponseBody
	public ResponseEntity<List<RuleResponse>> getUserRules()
	{
		User user = userService.getCurrentUser();
        List<Rule> userRules = rulesService.getUserRules(user.getId());
        List<RuleResponse> result = userRules.stream().map(ruleMapper::toEntity).collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	@GetMapping("/{id}")
	@ResponseBody
	public ResponseEntity<RuleResponse> getRule(@Valid @PathVariable String id)
	{
		User user = userService.getCurrentUser();
		RuleResponse result = ruleMapper.toEntity(rulesService.getRule(Integer.parseInt(id), user.getId()));

		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteRule(@Valid @PathVariable String id)
	{
		User user = userService.getCurrentUser();
		rulesService.deleteRule(Integer.parseInt(id), user.getId());

		return ResponseEntity.ok().build();
	}

	@PostMapping("/")
	public ResponseEntity<RuleResponse> save(@Valid @RequestBody RuleRequest userRule)
	{
		User user = userService.getCurrentUser();
		Rule result = rulesService.saveRule(user, ruleRequestMapper.toDto(userRule));

		return ResponseEntity.ok(ruleMapper.toEntity(result));
	}

    @PostMapping("/actions/")
    public ResponseEntity<?> addAction(@Valid @RequestBody RuleActionRequest ruleActionRequest)
    {
        User user = userService.getCurrentUser();
        Rule rule = rulesService.getRule(Math.toIntExact(ruleActionRequest.getRule_id()), user.getId());
        
        if (rule == null || rule.getId() == null) {
            return ResponseEntity
                  .badRequest()
                  .body(new MessageResponse(translate.get("rules.conditions.missing-parameters")));
        }

        RuleAction ruleAction = ruleActionRequestMapper.toDto(ruleActionRequest);
        ruleAction.setRule(rule);

        rule.getRuleActions().add(rulesService.saveAction(ruleAction));        
        return ResponseEntity.ok(rule.getRuleActions());
    }

    @PostMapping("/conditions/")
    public ResponseEntity<?> addCondition(@Valid @RequestBody RuleConditionRequest ruleConditionRequest)
    {
        User user = userService.getCurrentUser();
        Rule rule = rulesService.getRule(ruleConditionRequest.getRule_id(), user.getId());

        if (rule == null || rule.getId() == null) {
            return ResponseEntity
                  .badRequest()
                  .body(new MessageResponse(translate.get("rules.conditions.missing-parameters")));
        }

        RuleCondition ruleCondition = ruleConditionRequestMapper.toDto(ruleConditionRequest);
        ruleCondition.setRule(rule);

        rule.getRuleConditions().add(rulesService.saveCondition(ruleCondition));

        return ResponseEntity.ok(rule.getRuleActions());
    }

    @DeleteMapping("/actions/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteRuleAction(@Valid @PathVariable Integer id)
    {
        User user = userService.getCurrentUser();
        RuleAction ruleAction = rulesService.getRuleAction(id);

        if (ruleAction == null || ! user.getId().equals(ruleAction.getRule().getUser().getId())) {
            return ResponseEntity
                  .badRequest()
                  .body(new MessageResponse(translate.get("rules.conditions.missing-parameters")));
        }
        
        rulesService.deleteRuleAction(id);
        
        return ResponseEntity.ok(rulesService.getRule(id, user.getId()).getRuleActions());
    }

    @DeleteMapping("/conditions/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteRuleCondition(@Valid @PathVariable Integer id)
    {
        User user = userService.getCurrentUser();
        Rule rule = rulesService.getRule(id, user.getId());

        if (rule == null || rule.getId() == null) {
            return ResponseEntity
                  .badRequest()
                  .body(new MessageResponse(translate.get("rules.conditions.missing-parameters")));
        }

        rulesService.deleteRuleCondition(id);

        rule = rulesService.getRule(id, user.getId());

        return ResponseEntity.ok(rule.getRuleActions());
    }
}

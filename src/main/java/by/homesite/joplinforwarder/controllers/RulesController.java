package by.homesite.joplinforwarder.controllers;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.controllers.dto.request.RuleRequest;
import by.homesite.joplinforwarder.controllers.dto.response.RuleResponse;
import by.homesite.joplinforwarder.controllers.mapper.RuleMapper;
import by.homesite.joplinforwarder.controllers.mapper.RuleRequestMapper;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.RulesService;
import by.homesite.joplinforwarder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rules")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class RulesController
{
	@Autowired
	UserService userService;

	@Autowired
	RulesService rulesService;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	RuleMapper ruleMapper;

	@Autowired
	RuleRequestMapper ruleRequestMapper;

	@GetMapping("/")
	@ResponseBody
	public ResponseEntity<List<RuleResponse>> getUserRules()
	{
		User user = userService.getCurrentUser();
		List<RuleResponse> result = rulesService.getUserRules(user.getId()).stream().map(ruleMapper::toEntity).collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	@PostMapping("/")
	public ResponseEntity<?> save(@Valid @RequestBody RuleRequest userRule)
	{
		User user = userService.getCurrentUser();
		Rule result = rulesService.saveRule(user, ruleRequestMapper.toDto(userRule));

		return ResponseEntity.ok(result);
	}

}

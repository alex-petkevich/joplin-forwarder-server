package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.RuleAction;
import by.homesite.joplinforwarder.model.RuleCondition;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.RuleConditionsRepository;
import by.homesite.joplinforwarder.repository.RulesActionsRepository;
import by.homesite.joplinforwarder.repository.RulesRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class RulesService
{
	private final RulesRepository rulesRepository;
    
	private final RulesActionsRepository ruleActionsRepository;
    
	private final RuleConditionsRepository ruleConditionsRepository;

    private final TranslateService translateService;

    public RulesService(RulesRepository rulesRepository, RulesActionsRepository ruleActionsRepository, RuleConditionsRepository ruleConditionsRepository, ApplicationProperties applicationProperties, TranslateService translateService) {
		this.rulesRepository = rulesRepository;
        this.ruleActionsRepository = ruleActionsRepository;
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.translateService = translateService;
    }

	public Rule saveRule(User user, Rule rule)
	{
		rule.setUser(user);
		return rulesRepository.save(rule);
	}

	public List<Rule> getUserRules(Integer userId)
	{
		return rulesRepository.getByUserIdOrderByPriority(userId);
	}

	public List<Rule> getUserActiveRules(Integer userId)
	{
		return rulesRepository.getByUserIdAndActiveOrderByPriority(userId, true);
	}

	public Rule getRule(Integer id, Integer userId)
	{
		return rulesRepository.getByIdAndUserId(id, userId);
	}

	public void deleteRule(Integer id, Integer userId)
	{
		Rule rule = rulesRepository.getByIdAndUserId(id, userId);
		if (rule != null) {
			rulesRepository.delete(rule);
            
            ruleActionsRepository.deleteByRuleId(id);
            ruleConditionsRepository.deleteByRuleId(id);
		}
	}
    
	public void copyRule(Integer id, Integer userId)
	{
		Rule rule = rulesRepository.getByIdAndUserId(id, userId);
		if (rule != null) {
            Rule newRule = new Rule();
            newRule.setName(rule.getName() + " - " + translateService.get("rules.copy-title"));
            newRule.setActive(rule.getActive());
            newRule.setProcessed(0);
            newRule.setDeleted(rule.getDeleted());
            newRule.setPriority(rule.getPriority());
            newRule.setSave_in(rule.getSave_in());
            newRule.setSave_in_parent_id(rule.getSave_in_parent_id());
            newRule.setStop_process_rules(rule.getStop_process_rules());
            newRule.setCreated_at(OffsetDateTime.now());
            Rule finalNewRule = this.saveRule(rule.getUser(), newRule);

            rule.getRuleConditions().forEach(condition ->{
                RuleCondition newCondition = new RuleCondition();
                newCondition.setType(condition.getType());
                newCondition.setComparison_method(condition.getComparison_method());
                newCondition.setComparison_text(condition.getComparison_text());
                newCondition.setCond(condition.getCond());
                newCondition.setRule(finalNewRule);
                newCondition.setCreated_at(OffsetDateTime.now());
                newCondition.setLast_modified_at(OffsetDateTime.now());
                ruleConditionsRepository.save(newCondition);
            });
            
            rule.getRuleActions().forEach(action ->{
                RuleAction newAction = new RuleAction();
                newAction.setAction(action.getAction());
                newAction.setAction_target(action.getAction_target());
                newAction.setRule(newRule);
                newAction.setCreated_at(OffsetDateTime.now());
                newAction.setLast_modified_at(OffsetDateTime.now());
                ruleActionsRepository.save(newAction);
            });
        }
	}
    
	public Rule getUserRule(Mail mail, User user)
	{
		List<Rule> rules = getUserActiveRules(user.getId());

		for (Rule rule : rules) {
            Boolean meets = null;
            for (RuleCondition ruleCondition: rule.getRuleConditions()) {
                boolean meetsRule = switch (ruleCondition.getType()) {
                    case "FROM" -> compareField(ruleCondition, mail.getSender());
                    case "TO" -> compareField(ruleCondition, mail.getRecipient());
                    case "SUBJECT" -> compareField(ruleCondition, mail.getSubject());
                    case "BODY" -> compareField(ruleCondition, mail.getText());
                    case "ATTACH" -> compareField(ruleCondition, mail.getAttachments());
                    default -> false;
                };
                meets =  ruleCondition.getCond() != null && ruleCondition.getCond() == 1 && meets != null ? meets & meetsRule : Boolean.TRUE.equals(
                      meets) | meetsRule;
            }

			if (Boolean.TRUE.equals(meets)) {
                return rule;
			}
		}

		return null;
	}

	private boolean compareField(RuleCondition rule, String subject) {
		boolean meets = false;
		subject = subject.trim();

		switch (rule.getComparison_method())
		{
			case "EQUALS" -> meets = rule.getComparison_text().equalsIgnoreCase(subject);
			case "NOT_EQUALS" -> meets = !rule.getComparison_text().equalsIgnoreCase(subject);
			case "CONTAINS" -> meets = prepareSearchStatement(rule, subject);
			case "NOT_CONTAINS" -> meets = !prepareSearchStatement(rule, subject);
		}

		return meets;
	}

	private static boolean prepareSearchStatement(RuleCondition rule, String subject) {
		String prepared = rule.getComparison_text().replace("*", "(.+)");
		prepared = prepared.replace("%", ".{1}");
		Pattern p = Pattern.compile(prepared, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(subject);
		return m.find();
	}

    public RuleAction saveAction(RuleAction ruleAction) {
        return ruleActionsRepository.save(ruleAction);
    }
    
    public RuleCondition saveCondition(RuleCondition ruleCondition) {
        return ruleConditionsRepository.save(ruleCondition);
    }

    public void deleteRuleAction(Integer id) {
        ruleActionsRepository.deleteById(Long.valueOf(id));
    }
    
    public void deleteRuleCondition(Integer id) {
        ruleConditionsRepository.deleteById(Long.valueOf(id));
    }

    public RuleAction getRuleAction(Integer id)
    {
        return ruleActionsRepository.getReferenceById(Long.valueOf(id));
    }

    public RuleCondition getRuleCondition(Integer id)
    {
        return ruleConditionsRepository.getReferenceById(Long.valueOf(id));
    }

}

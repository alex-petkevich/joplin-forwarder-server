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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RulesService
{
	private final RulesRepository rulesRepository;
    
	private final RulesActionsRepository ruleActionsRepository;
    
	private final RuleConditionsRepository ruleConditionsRepository;

	private final ApplicationProperties applicationProperties;

	public RulesService(RulesRepository rulesRepository, RulesActionsRepository ruleActionsRepository, RuleConditionsRepository ruleConditionsRepository, ApplicationProperties applicationProperties) {
		this.rulesRepository = rulesRepository;
        this.ruleActionsRepository = ruleActionsRepository;
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.applicationProperties = applicationProperties;
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

	public boolean meetsUserRule(Mail mail, User user) {
		return true;
	}

	public Rule getUserRule(Mail mail, User user)
	{
		List<Rule> rules = getUserActiveRules(user.getId());

        Boolean meets = null;
		for (Rule rule : rules) {
            for (RuleCondition ruleCondition: rule.getRuleConditions()) {
                boolean meetsRule = switch (ruleCondition.getType()) {
                    case "FROM" -> compareField(ruleCondition, mail.getSender());
                    case "TO" -> compareField(ruleCondition, mail.getRecipient());
                    case "SUBJECT" -> compareField(ruleCondition, mail.getSubject());
                    case "BODY" -> compareField(ruleCondition, mail.getText());
                    case "ATTACH" -> compareField(ruleCondition, mail.getAttachments());
                    default -> false;
                };
                if (meets == null) {
                    meets = meetsRule;
                }
                meets = ruleCondition.getCond() == 1 ? meets & meetsRule : meets | meetsRule;
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

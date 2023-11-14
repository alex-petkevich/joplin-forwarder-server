package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.RulesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RulesService
{
	@Autowired
	private RulesRepository rulesRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

	public Rule saveRule(User user, Rule rule)
	{
		rule.setUser(user);
		return rulesRepository.save(rule);
	}

	public List<Rule> getUserRules(Long userId)
	{
		return rulesRepository.getByUserId(userId);
	}

	public List<Rule> getUserActiveRules(Long userId)
	{
		return rulesRepository.getByUserIdAndActiveOrderByPriority(userId, true);
	}

	public Rule getRule(Integer id, Long userId)
	{
		return rulesRepository.getByIdAndUserId(id, userId);
	}

	public void deleteRule(Integer id, Long userId)
	{
		Rule rule = rulesRepository.getByIdAndUserId(id, userId);
		if (rule != null) {
			rulesRepository.delete(rule);
		}
	}

	public boolean meetsUserRule(Mail mail, User user) {
		return true;
	}

	public Rule getUserRule(Mail mail, User user)
	{
		List<Rule> rules = getUserActiveRules(user.getId());

		for (Rule rule : rules) {
			boolean meetsRule = switch (rule.getType())
					{
						case "FROM" -> compareField(rule, mail.getSender());
						case "TO" -> compareField(rule, mail.getRecipient());
						case "SUBJECT" -> compareField(rule, mail.getSubject());
						case "BODY" -> compareField(rule, mail.getText());
						case "ATTACH" -> compareField(rule, mail.getAttachments());
						default -> false;
					};

			if (meetsRule) {
				return rule;
			}
		}

		return null;
	}

	private boolean compareField(Rule rule, String subject) {
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

	private static boolean prepareSearchStatement(Rule rule, String subject) {
		String prepared = rule.getComparison_text().replace("*", "(.+)");
		prepared = prepared.replace("%", ".{1}");
		Pattern p = Pattern.compile(prepared);
		Matcher m = p.matcher(subject);
		return m.find();
	}
}

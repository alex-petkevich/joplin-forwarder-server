package by.homesite.joplinforwarder.service;

import antlr.StringUtils;
import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.RulesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
		List<Rule> rules = getUserRules(user.getId());

		for (Rule rule : rules) {
			boolean meetsRule = false;

			switch (rule.getType()) {
				case "FROM":
					meetsRule = compareField(rule, mail.getSender());
					break;
				case "TO":
					meetsRule = compareField(rule, mail.getRecipient());
					break;
				case "SUBJECT":
					meetsRule = compareField(rule, mail.getSubject());
					break;
				case "BODY":
					meetsRule = compareField(rule, mail.getText());
					break;
				case "ATTACH":
					meetsRule = compareField(rule, mail.getAttachments());
					break;
			}

			if (meetsRule) {
				return rule;
			}
		}

		return null;
	}

	private boolean compareField(Rule rule, String subject) {
		boolean meets = false;
		subject = subject.trim();

		switch (rule.getComparison_method()) {
			case "EQUALS":
				meets = rule.getComparison_text().equalsIgnoreCase(subject);
				break;
			case "NOT_EQUALS":
				meets = !rule.getComparison_text().equalsIgnoreCase(subject);
				break;
			case "CONTAINS":
				meets = subject.matches(prepareSearchStatement(rule));
				break;
			case "NOT_CONTAINS":
				meets = !subject.matches(prepareSearchStatement(rule));
				break;
		}

		return meets;
	}

	private static String prepareSearchStatement(Rule rule) {
		String prepared = rule.getComparison_text().replace("*", ".+");
		prepared = prepared.replace("%", ".{1}");
		return prepared;
	}
}

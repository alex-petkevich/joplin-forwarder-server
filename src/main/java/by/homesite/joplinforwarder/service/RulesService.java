package by.homesite.joplinforwarder.service;

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
}

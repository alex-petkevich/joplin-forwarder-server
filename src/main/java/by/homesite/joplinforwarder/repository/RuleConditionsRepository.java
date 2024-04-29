package by.homesite.joplinforwarder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.RuleCondition;

@Repository
public interface RuleConditionsRepository extends JpaRepository<RuleCondition, Long>
{
    void deleteByRuleId(Integer id);
}

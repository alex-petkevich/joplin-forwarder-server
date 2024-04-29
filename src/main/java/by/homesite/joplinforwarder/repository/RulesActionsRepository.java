package by.homesite.joplinforwarder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.homesite.joplinforwarder.model.RuleAction;

@Repository
public interface RulesActionsRepository extends JpaRepository<RuleAction, Long>
{
    void deleteByRuleId(Integer id);
}

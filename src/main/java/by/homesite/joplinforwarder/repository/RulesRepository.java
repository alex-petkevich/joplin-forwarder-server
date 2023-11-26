package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulesRepository extends JpaRepository<Rule, Long>
{
    List<Rule> getByUserIdOrderByPriority(Integer userId);

    List<Rule> getByUserIdAndActiveOrderByPriority(Integer userId, Boolean active);

    Rule getByIdAndUserId(Integer id, Integer userId);
}

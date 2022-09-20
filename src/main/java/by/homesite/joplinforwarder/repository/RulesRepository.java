package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulesRepository extends JpaRepository<Rule, Long>
{
    List<Rule> getByUserId(Long userId);

    Rule getByUserIdAndName(Long userId, String name);
}

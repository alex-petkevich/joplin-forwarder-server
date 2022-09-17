package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long>
{
    List<Settings> getByUserId(Long userId);

    Settings getByUserIdAndName(Long userId, String name);
}

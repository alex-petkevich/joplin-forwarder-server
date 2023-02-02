package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long>
{
    List<Settings> getByUserId(Long userId);

    Settings getByUserIdAndName(Long userId, String name);

    @Query("SELECT user FROM Settings WHERE name = 'period' AND value != ''")
    List<User> getUserWithEmailSettings();
}

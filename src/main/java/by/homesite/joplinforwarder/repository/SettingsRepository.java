package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long>
{
    String SETTINGS_EMAIL_CACHE = "settingsEmailCache";
    List<Settings> getByUserId(Long userId);

    Settings getByUserIdAndName(Long userId, String name);

    @Cacheable(cacheNames = SETTINGS_EMAIL_CACHE)
    @Query("SELECT user FROM Settings WHERE name = 'period' AND value != ''")
    List<User> getUserWithEmailSettings();
}

package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class SettingsService
{
	@Autowired
	private SettingsRepository settingsRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

	public void saveSettings(User user, HashMap<String, String> values)
	{
		values.forEach((name, value) -> {
				Settings currentSetting = settingsRepository.getByUserIdAndName(user.getId(), name);
				if (currentSetting == null) {
					currentSetting = new Settings();
					currentSetting.setName(name);
					currentSetting.setCreatedAt(OffsetDateTime.now());
					currentSetting.setUser(user);
				}
				currentSetting.setValue(value);
				currentSetting.setLastModifiedAt(OffsetDateTime.now());
				settingsRepository.save(currentSetting);
		});

	}

	public List<Settings> getUserSettings(Long userId)
	{

		return settingsRepository.getByUserId(userId);
	}

}

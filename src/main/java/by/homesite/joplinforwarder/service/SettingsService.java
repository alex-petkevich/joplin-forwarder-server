package by.homesite.joplinforwarder.service;

import static by.homesite.joplinforwarder.util.GlobUtil.settingValue;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService
{
	private final SettingsRepository settingsRepository;

	private final ApplicationProperties applicationProperties;

	public SettingsService(SettingsRepository settingsRepository, ApplicationProperties applicationProperties) {
		this.settingsRepository = settingsRepository;
		this.applicationProperties = applicationProperties;
	}

	public void saveSettings(User user, Map<String, String> values)
	{
		values.forEach((name, value) -> {
			this.setSettingValue(user, name, value);
		});

	}

	public Settings setSettingValue(User user, String name, String value) {
		Settings currentSetting = settingsRepository.getByUserIdAndName(user.getId(), name);
		if (currentSetting == null) {
			currentSetting = new Settings();
			currentSetting.setName(name);
			currentSetting.setCreatedAt(OffsetDateTime.now());
			currentSetting.setUser(user);
		}
		currentSetting.setValue(value);
		currentSetting.setLastModifiedAt(OffsetDateTime.now());
		return settingsRepository.save(currentSetting);
	}

	public List<Settings> getUserSettings(Integer userId)
	{

		return settingsRepository.getByUserId(userId);
	}

	public List<User> getMailSettingsByUsers() {
		List<User> users = settingsRepository.getUserWithEmailSettings();

		return users.stream().filter(it -> {
			String lastTimeProcessed = settingValue(it, "lasttime_mail_processed");
			String period = settingValue(it, "period");
			return "".equals(lastTimeProcessed) 
                    || null == lastTimeProcessed
					|| OffsetDateTime.now().toEpochSecond() > Long.parseLong(lastTimeProcessed) + Long.parseLong(period) * 60;
		}).toList();
	}
}

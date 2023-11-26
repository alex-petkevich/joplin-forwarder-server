package by.homesite.joplinforwarder.controllers;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.controllers.dto.response.SettingsResponse;
import by.homesite.joplinforwarder.controllers.mapper.SettingsMapper;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class SettingsController
{
	final
	UserService userService;

	final
	SettingsService settingsService;

	final
	ApplicationProperties applicationProperties;

	final
	SettingsMapper settingsMapper;

	public SettingsController(UserService userService, SettingsService settingsService, ApplicationProperties applicationProperties, SettingsMapper settingsMapper) {
		this.userService = userService;
		this.settingsService = settingsService;
		this.applicationProperties = applicationProperties;
		this.settingsMapper = settingsMapper;
	}

	@GetMapping("/")
	@ResponseBody
	public ResponseEntity<List<SettingsResponse>> getUserSettings()
	{
		User user = userService.getCurrentUser();
		List<SettingsResponse> result = settingsService.getUserSettings(user.getId()).stream().map(settingsMapper::toEntity).toList();

		return ResponseEntity.ok(result);
	}

	@PostMapping("/")
	public ResponseEntity<?> save(@Valid @RequestBody Map<String, String> userSettings)
	{
		User user = userService.getCurrentUser();
		settingsService.saveSettings(user, userSettings);

		return ResponseEntity.ok().build();
	}

}

package by.homesite.joplinforwarder.controllers;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import by.homesite.joplinforwarder.controllers.dto.request.ActivateRequest;
import by.homesite.joplinforwarder.model.ERole;
import by.homesite.joplinforwarder.model.Role;
import by.homesite.joplinforwarder.controllers.dto.request.LoginRequest;
import by.homesite.joplinforwarder.controllers.dto.request.SignupRequest;
import by.homesite.joplinforwarder.controllers.dto.response.JwtResponse;
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.repository.RoleRepository;
import by.homesite.joplinforwarder.service.UserService;

@RestController
@RequestMapping("/api/account")
public class AccountController
{
	@Autowired
	RoleRepository roleRepository;

	@Autowired
	UserService userService;

	@Autowired
	private MessageSource messageSource;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
	{
		JwtResponse response = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest)
	{
		if (userService.isUsernameExists(signUpRequest.getUsername()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(messageSource.getMessage("account.register-user.username-exists", null, LocaleContextHolder.getLocale())));
		}
		if (userService.isEmailExists(signUpRequest.getEmail()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(messageSource.getMessage("account.register-user.email-exists", null, LocaleContextHolder.getLocale())));
		}
		
		// Create new user's account
		Set<Role> roles = getUserRoles(signUpRequest.getRole());

		userService.createUser(signUpRequest, roles);
		
		return ResponseEntity.ok(new MessageResponse(messageSource.getMessage("account.register-user.user-registered", null, LocaleContextHolder.getLocale())));
	}

	@PostMapping("/activate")
	public ResponseEntity<?> activateUser(@Valid @RequestBody ActivateRequest activateRequest)
	{
		JwtResponse response = userService.activate(activateRequest.getKey());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/forgot-password/{key}")
	public ResponseEntity<?> forgotPassword(@Valid @PathVariable String key)
	{
		MessageResponse response = userService.forgotPasswordSend(key);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/password-reset")
	public ResponseEntity<?> passwordReset(@Valid @RequestBody LoginRequest loginRequest)
	{
		MessageResponse response = userService.changePassword(loginRequest.getUsername(), loginRequest.getPassword());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/check-key")
	public ResponseEntity<?> checkKey(@Valid @RequestBody ActivateRequest activateRequest)
	{
		MessageResponse response = userService.checkResetKey(activateRequest.getKey());

		return ResponseEntity.ok(response);
	}

	private Set<Role> getUserRoles(Set<String> roleList)
	{
		Set<Role> roles = new HashSet<>();
		if (roleList == null)
		{
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException(messageSource.getMessage("account.register-user.role-not-found", null, LocaleContextHolder.getLocale())));
			roles.add(userRole);
		}
		else
		{
			roleList.forEach(role -> {
				switch (role)
				{
					case "admin":
						Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new RuntimeException(messageSource.getMessage("account.register-user.role-not-found", null, LocaleContextHolder.getLocale())));
						roles.add(adminRole);
						break;
					default:
						Role userRole = roleRepository.findByName(ERole.ROLE_USER)
								.orElseThrow(() -> new RuntimeException(messageSource.getMessage("account.register-user.role-not-found", null, LocaleContextHolder.getLocale())));
						roles.add(userRole);
				}
			});
		}
		
		return roles;
	}
}

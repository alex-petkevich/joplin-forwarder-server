package by.homesite.joplinforwarder.controllers;

import java.util.HashSet;
import java.util.Set;

import by.homesite.joplinforwarder.controllers.dto.request.UserRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import by.homesite.joplinforwarder.controllers.dto.request.ActivateRequest;
import by.homesite.joplinforwarder.controllers.dto.request.LoginRequest;
import by.homesite.joplinforwarder.controllers.dto.response.JwtResponse;
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.model.ERole;
import by.homesite.joplinforwarder.model.Role;
import by.homesite.joplinforwarder.repository.RoleRepository;
import by.homesite.joplinforwarder.service.TranslateService;
import by.homesite.joplinforwarder.service.UserService;

@RestController
@RequestMapping("/api/account")
public class AccountController
{
	final
	RoleRepository roleRepository;

	final
	UserService userService;
	
	final
	TranslateService translate;

	public AccountController(RoleRepository roleRepository, UserService userService, TranslateService translate) {
		this.roleRepository = roleRepository;
		this.userService = userService;
		this.translate = translate;
	}

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
	{
		JwtResponse response = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest signUpRequest)
	{
		if (userService.isUsernameExists(signUpRequest.getUsername()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(translate.get("account.register-user.username-exists")));
		}
		if (userService.isEmailExists(signUpRequest.getEmail()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(translate.get("account.register-user.email-exists")));
		}
		
		// Create new user's account
		Set<Role> roles = new HashSet<>();
		roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException(translate.get("account.register-user.role-not-found"))));

		userService.createUser(signUpRequest, roles);
		
		return ResponseEntity.ok(new MessageResponse(translate.get("account.register-user.user-registered")));
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

}

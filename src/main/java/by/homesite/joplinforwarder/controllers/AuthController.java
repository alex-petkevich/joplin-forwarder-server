package by.homesite.joplinforwarder.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import by.homesite.joplinforwarder.model.ERole;
import by.homesite.joplinforwarder.model.Role;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.controllers.dto.request.LoginRequest;
import by.homesite.joplinforwarder.controllers.dto.request.SignupRequest;
import by.homesite.joplinforwarder.controllers.dto.response.JwtResponse;
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.repository.RoleRepository;
import by.homesite.joplinforwarder.repository.UserRepository;
import by.homesite.joplinforwarder.security.jwt.JwtUtils;
import by.homesite.joplinforwarder.security.services.UserDetailsImpl;
import by.homesite.joplinforwarder.service.UserService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController
{
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	UserService userService;
	
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
					.body(new MessageResponse("Error: Username is already taken!"));
		}
		if (userService.isEmailExists(signUpRequest.getEmail()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}
		
		// Create new user's account
		Set<Role> roles = getUserRoles(signUpRequest.getRole());

		userService.saveUser(signUpRequest, roles);
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	private Set<Role> getUserRoles(Set<String> roleList)
	{
		Set<Role> roles = new HashSet<>();
		if (roleList == null)
		{
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		}
		else
		{
			roleList.forEach(role -> {
				switch (role)
				{
					case "admin":
						Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(adminRole);
						break;
					default:
						Role userRole = roleRepository.findByName(ERole.ROLE_USER)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(userRole);
				}
			});
		}
		
		return roles;
	}
}

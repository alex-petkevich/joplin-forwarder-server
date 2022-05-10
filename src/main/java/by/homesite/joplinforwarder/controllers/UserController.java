package by.homesite.joplinforwarder.controllers;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import by.homesite.joplinforwarder.controllers.dto.request.SignupRequest;
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.controllers.dto.response.UserResponse;
import by.homesite.joplinforwarder.controllers.mapper.UserMapper;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.UserService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
	
	private final UserService userService;
	private final UserMapper userMapper;

	public UserController(UserService userService, UserMapper userMapper)
	{
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@PostMapping("/")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> saveUser(@Valid @RequestBody SignupRequest userRequest) {
		User currentUserData = userService.getCurrentUser();

		if (!userRequest.getUsername().equals(currentUserData.getUsername()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username change is not supported"));
		}
		if (!userRequest.getEmail().equals(currentUserData.getEmail()) && userService.isEmailExists(userRequest.getEmail()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		userRequest.setUsername(currentUserData.getUsername());
		userService.saveUser(currentUserData, userRequest, null);

		return ResponseEntity.ok(new MessageResponse("User saved successfully"));
	}

	@GetMapping("/")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public UserResponse getUserInfo() {
		User user = userService.getCurrentUser();

		return userMapper.toEntity(user);
	}
	
	@GetMapping("/all")
	public String allAccess() {
		return "Public Content.";
	}

	@GetMapping("/user")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public String userAccess() {
		return "User Content.";
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return "Admin Board.";
	}
}

package by.homesite.joplinforwarder.controllers;

import javax.validation.Valid;

import by.homesite.joplinforwarder.controllers.mapper.UserSignupRequestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	private final UserService userService;
	private final UserMapper userMapper;
	private final UserSignupRequestMapper userSignupRequestMapper;

	public UserController(UserService userService, UserMapper userMapper, UserSignupRequestMapper userSignupRequestMapper)
	{
		this.userService = userService;
		this.userMapper = userMapper;
		this.userSignupRequestMapper = userSignupRequestMapper;
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

	@PostMapping("/lang")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> saveUserLang(@RequestBody SignupRequest userRequest) {
		User currentUserData = userService.getCurrentUser();

		if (currentUserData == null) {
			return ResponseEntity.notFound().build();
		}

		SignupRequest signupRequest = userSignupRequestMapper.toEntity(currentUserData);
		signupRequest.setLang(userRequest.getLang());

		userService.saveUser(currentUserData, signupRequest, null);

		return ResponseEntity.ok(new MessageResponse("Lang updated successfully"));
	}

}

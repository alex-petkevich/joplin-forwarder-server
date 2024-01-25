package by.homesite.joplinforwarder.controllers;

import by.homesite.joplinforwarder.controllers.dto.request.UserRequest;
import by.homesite.joplinforwarder.controllers.dto.response.RoleResponse;
import by.homesite.joplinforwarder.controllers.mapper.RoleMapper;
import by.homesite.joplinforwarder.model.Role;
import by.homesite.joplinforwarder.util.ControllerUtil;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.controllers.dto.response.UserResponse;
import by.homesite.joplinforwarder.controllers.mapper.UserMapper;
import by.homesite.joplinforwarder.controllers.mapper.UserSignupRequestMapper;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.TranslateService;
import by.homesite.joplinforwarder.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	private final UserService userService;
	private final UserMapper userMapper;
	private final RoleMapper roleMapper;
	private final UserSignupRequestMapper userSignupRequestMapper;

	final
	TranslateService translate;

	public UserController(UserService userService, UserMapper userMapper, RoleMapper roleMapper, UserSignupRequestMapper userSignupRequestMapper, TranslateService translate)
	{
		this.userService = userService;
		this.userMapper = userMapper;
		this.roleMapper = roleMapper;
		this.userSignupRequestMapper = userSignupRequestMapper;
		this.translate = translate;
	}

	@PostMapping("/")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> saveUser(@Valid @RequestBody UserRequest userRequest) {
		User currentUserData = userService.getCurrentUser();

		if (!userRequest.getUsername().equals(currentUserData.getUsername()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(translate.get("user.error-not-supported")));
		}
		if (!userRequest.getEmail().equals(currentUserData.getEmail()) && userService.isEmailExists(userRequest.getEmail()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(translate.get("user.error-email-already-use")));
		}

		userRequest.setUsername(currentUserData.getUsername());
		userService.saveUser(currentUserData, userRequest, null);

		return ResponseEntity.ok(new MessageResponse(translate.get("user.saved-successfully")));
	}

	@GetMapping("/")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public UserResponse getUserInfo() {
		User user = userService.getCurrentUser();

		return userMapper.toEntity(user);
	}

	@PostMapping("/lang")
	public ResponseEntity<?> saveUserLang(@RequestBody UserRequest userRequest) {
		User currentUserData = userService.getCurrentUser();

		if (currentUserData == null) {
			return ResponseEntity.notFound().build();
		}

		UserRequest signupRequest = userSignupRequestMapper.toEntity(currentUserData);
		signupRequest.setLang(userRequest.getLang());

		userService.saveUser(currentUserData, signupRequest, null);

		return ResponseEntity.ok(new MessageResponse(translate.get("user.lang-updated-successfully")));
	}

	@GetMapping("/admin/")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getUsers(@RequestParam(required = false) String name,
										@RequestParam(required = false)  String username,
										@RequestParam(required = false)  String email,
										@RequestParam (required = false) String role,
										@RequestParam (required = false) Boolean active,
									   @RequestParam(defaultValue = "0") int page,
									   @RequestParam(defaultValue = "createdAt-desc") String sort) {

		Pageable paging = PageRequest.of(page, 20, ControllerUtil.getSortOrder(sort));

		Page<UserResponse> users = userService.getUsers(name, username, email, role, active, paging).map(userMapper::toEntity);

		return ResponseEntity.ok(users);
	}

	@PostMapping("/admin/activate/")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> activateUser(@RequestBody Long userId) {

		UserResponse user = userMapper.toEntity(userService.adminUserActivation(userId));

		return ResponseEntity.ok(user);
	}

	@GetMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {

		UserResponse user = userMapper.toEntity(userService.getUser(id));

		return ResponseEntity.ok(user);
	}

	@GetMapping("/admin/roles/")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<RoleResponse>> getRoles() {

		List<RoleResponse> roles = userService.getRoles().stream().map(roleMapper::toEntity).toList();

		return ResponseEntity.ok(roles);
	}

	@PostMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<MessageResponse> saveUserAdmin(@PathVariable(required = true)  Long id, @Valid @RequestBody UserRequest userRequest) {
		User userData = userService.getUser(id);

		if (!userRequest.getEmail().equals(userData.getEmail()) && userService.isEmailExists(userRequest.getEmail()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(translate.get("user.error-email-already-use")));
		}
		if (!userRequest.getUsername().equals(userData.getUsername()) && userService.isUsernameExists(userRequest.getUsername()))
		{
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(translate.get("user.error-username-already-use")));
		}

		userService.saveUser(userData, userRequest, userRequest.getRoles());

		return ResponseEntity.ok(new MessageResponse(translate.get("user.saved-successfully")));
	}
}

package by.homesite.joplinforwarder.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import by.homesite.joplinforwarder.controllers.dto.request.UserRequest;
import by.homesite.joplinforwarder.model.specification.UserSpecification;
import by.homesite.joplinforwarder.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.controllers.dto.response.JwtResponse;
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.model.Role;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.UserRepository;
import by.homesite.joplinforwarder.security.jwt.JwtUtils;
import by.homesite.joplinforwarder.security.services.UserDetailsImpl;

@Service
public class UserService
{
	public static final String SUCCESSFUL = "successful";
	final AuthenticationManager authenticationManager;
	final JwtUtils jwtUtils;
	final UserRepository userRepository;
	final RoleRepository roleRepository;
	final PasswordEncoder encoder;
	private final SendMailService sendMailService;

	private final ApplicationProperties applicationProperties;

	public UserService(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository,
					   RoleRepository roleRepository, PasswordEncoder encoder, SendMailService sendMailService, ApplicationProperties applicationProperties)
	{
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.encoder = encoder;
		this.sendMailService = sendMailService;
		this.applicationProperties = applicationProperties;
	}

	public JwtResponse authenticate(String username, String password)
	{
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, password));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		return new JwtResponse(jwt,
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				userDetails.getLang(),
				roles);
	}

	public boolean isUsernameExists(String username)
	{
		return userRepository.existsByUsername(username);
	}

	public boolean isEmailExists(String email)
	{
		return userRepository.existsByEmail(email);
	}

	public void createUser(UserRequest signUpRequest, Set<Role> roles)
	{
		User user = new User();
		user.setUsername(signUpRequest.getUsername());
		user.setEmail(signUpRequest.getEmail());
		user.setFirstname(signUpRequest.getFirstname());
		user.setLastname(signUpRequest.getLastname());
		user.setActive(0);
		user.setCreatedAt(OffsetDateTime.now());
		user.setLastModifiedAt(OffsetDateTime.now());
		user.setLang(applicationProperties.getGeneral().getDefaultLang());
		user.setActivationKey(generateActivationKey());	
		if (roles != null)
		{
			user.setRoles(roles);
		}
		userRepository.save(user);
		
		sendMailService.sendActivationEmail(user);
	}

	public void saveUser(User currentUserData, UserRequest signUpRequest, Set<Role> roles)
	{
		currentUserData.setUsername(signUpRequest.getUsername());
		currentUserData.setEmail(signUpRequest.getEmail());
		currentUserData.setFirstname(signUpRequest.getFirstname());
		currentUserData.setLastname(signUpRequest.getLastname());
		currentUserData.setLastModifiedAt(OffsetDateTime.now());
		currentUserData.setActive(Boolean.TRUE.equals(signUpRequest.getActive()) ? 1 : 0);
		if (StringUtils.hasText(signUpRequest.getLang()))
		{
			currentUserData.setLang(signUpRequest.getLang());
		}
		if (roles != null)
		{
			currentUserData.setRoles(roles);
		}
		userRepository.save(currentUserData);
	}

	private String generateActivationKey()
	{
		return UUID.randomUUID().toString();
	}

	public JwtResponse activate(String key)
	{
		User user = userRepository.findByActivationKey(key).orElse(null);
		if (user != null) {
			user.setActivationKey("");
			user.setActive(1);
			user.setLastModifiedAt(OffsetDateTime.now());
			userRepository.save(user);

			this.forgotPasswordSend(user.getEmail());

			return new JwtResponse(null, user.getId(), user.getUsername(), user.getEmail(), user.getLang(), null);
		}
		
		return new JwtResponse(null, null, null, null, null, null);
	}

	public MessageResponse forgotPasswordSend(String key)
	{
		User user = userRepository.findForResetPassword(key).orElse(null);
		if (user != null) {
			user.setActivationKey(generateActivationKey());
			user.setLastModifiedAt(OffsetDateTime.now());
			userRepository.save(user);

			sendMailService.sendPasswordResetMail(user);
			return new MessageResponse(SUCCESSFUL);
		}

		return new MessageResponse("");
	}

	public MessageResponse checkResetKey(String key)
	{
		User user = userRepository.findByActivationKey(key).orElse(null);
		if (user != null && user.getActive() == 1) {
			return new MessageResponse(SUCCESSFUL);
		}
		return new MessageResponse("");
	}

	public MessageResponse changePassword(String username, String password)
	{
		User user = userRepository.findByActivationKey(username).orElse(null);
		if (user != null) {
			user.setPassword(encoder.encode(password));
			user.setActivationKey("");
			user.setLastModifiedAt(OffsetDateTime.now());
			userRepository.save(user);
			
			return new MessageResponse(SUCCESSFUL);
		}
		
		return new MessageResponse("");
	}

	public User getCurrentUser()
	{
		if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			return null;
		}
		
		UserDetailsImpl printcipal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return userRepository.findByUsername(printcipal.getUsername()).orElse(null);
	}

	public Page<User> getUsers(String name, String username, String email, String role, Boolean active, Pageable pageable)
	{
		Specification<User> spec = Specification.where(null);

		if (StringUtils.hasText(username)) {
			spec = spec.and(UserSpecification.hasUsername(username));
		}
		if (StringUtils.hasText(name)) {
			spec = spec.and(UserSpecification.hasName(name));
		}
		if (StringUtils.hasText(email)) {
			spec = spec.and(UserSpecification.hasEmail(email));
		}
		if (StringUtils.hasText(role)) {
			spec = spec.and(UserSpecification.hasRole(role));
		}
		if (Boolean.TRUE.equals(active)) {
			spec = spec.and(UserSpecification.hasActive(active));
		}

		return userRepository.findAll(spec, pageable);
	}

	public User adminUserActivation(Long userId) {
		Optional<User> user = userRepository.findById(userId);
		if (user.isEmpty()) {
			return null;
		}

		User saveUser = user.get();
		saveUser.setActive(saveUser.getActive() != null && saveUser.getActive() == 0 ? 1 : 0);
		return userRepository.save(saveUser);
	}

	public User getUser(Long userId)
	{
		return userRepository.findById(userId).orElse(null);
	}

	public List<Role> getRoles()
	{
		return roleRepository.findAll();
	}

	public void deleteUser(Long id) {
		if (id != null) {
			userRepository.deleteById(id);
		}
	}
}

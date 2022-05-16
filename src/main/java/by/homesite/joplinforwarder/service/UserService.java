package by.homesite.joplinforwarder.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import by.homesite.joplinforwarder.controllers.dto.request.SignupRequest;
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
	final AuthenticationManager authenticationManager;
	final JwtUtils jwtUtils;
	final UserRepository userRepository;
	final PasswordEncoder encoder;
	private MailService mailService;

	@Value("${joplinforwarder.app.default_lang}")
	private String defaultLang;

	public UserService(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository,
			PasswordEncoder encoder, MailService mailService)
	{
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.userRepository = userRepository;
		this.encoder = encoder;
		this.mailService = mailService;
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

	public void createUser(SignupRequest signUpRequest, Set<Role> roles)
	{
		User user = new User();
		user.setUsername(signUpRequest.getUsername());
		user.setPassword(encoder.encode(signUpRequest.getPassword()));
		user.setEmail(signUpRequest.getEmail());
		user.setFirstname(signUpRequest.getFirstname());
		user.setLastname(signUpRequest.getLastname());
		user.setActive(0);
		user.setCreatedAt(OffsetDateTime.now());
		user.setLastModifiedAt(OffsetDateTime.now());
		user.setLang(this.defaultLang);
		user.setActivationKey(generateActivationKey());	
		if (roles != null)
		{
			user.setRoles(roles);
		}
		userRepository.save(user);
		
		mailService.sendActivationEmail(user);
	}

	public void saveUser(User currentUserData, SignupRequest signUpRequest, Set<Role> roles)
	{
		currentUserData.setUsername(signUpRequest.getUsername());
		currentUserData.setEmail(signUpRequest.getEmail());
		currentUserData.setFirstname(signUpRequest.getFirstname());
		currentUserData.setLastname(signUpRequest.getLastname());
		currentUserData.setLastModifiedAt(OffsetDateTime.now());
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
			return new JwtResponse(null, user.getId(), user.getUsername(), user.getEmail(), null);
		}
		
		return new JwtResponse(null, null, null, null, null);
	}

	public MessageResponse forgotPasswordSend(String key)
	{
		User user = userRepository.findForResetPassword(key).orElse(null);
		if (user != null) {
			user.setActivationKey(generateActivationKey());
			user.setLastModifiedAt(OffsetDateTime.now());
			userRepository.save(user);

			mailService.sendPasswordResetMail(user);
			return new MessageResponse("successful");
		}

		return new MessageResponse("");
	}

	public MessageResponse checkResetKey(String key)
	{
		User user = userRepository.findByActivationKey(key).orElse(null);
		if (user != null && user.getActive() == 1) {
			return new MessageResponse("successful");
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
			
			return new MessageResponse("successful");
		}
		
		return new MessageResponse("");
	}

	public User getCurrentUser()
	{
		if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			return null;
		}
		
		UserDetailsImpl printcipal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = userRepository.findByUsername(printcipal.getUsername()).orElse(null);
		
		return user;
	}
}

package by.homesite.joplinforwarder.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import by.homesite.joplinforwarder.controllers.dto.request.SignupRequest;
import by.homesite.joplinforwarder.controllers.dto.response.JwtResponse;
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

	public void saveUser(SignupRequest signUpRequest, Set<Role> roles)
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
		user.setRoles(roles);
		userRepository.save(user);
		
		mailService.sendActivationEmail(user);
	}

	private String generateActivationKey()
	{
		return UUID.randomUUID().toString();
	}
}

package by.homesite.joplinforwarder.security.services;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import by.homesite.joplinforwarder.model.User;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class UserDetailsImpl implements UserDetails
{
	@Serial
	private static final long serialVersionUID = 1L;
	private final Long id;
	private final String username;
	private final String email;
	@JsonIgnore
	private final String password;
	private final Boolean active;
	private final String lang;
	private final Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(Long id, String username, String email, String password, Boolean active, String lang,
			Collection<? extends GrantedAuthority> authorities)
	{
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.authorities = authorities;
		this.active = active;
		this.lang = lang;
	}

	public static UserDetailsImpl build(User user)
	{
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toList());
		return new UserDetailsImpl(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				user.getActive() == 1,
				user.getLang(),
				authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return authorities;
	}

	public Long getId()
	{
		return id;
	}

	public String getEmail()
	{
		return email;
	}

	@Override
	public String getPassword()
	{
		return password;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return true;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return active;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return active;
	}

	public String getLang()
	{
		return lang;
	}
}

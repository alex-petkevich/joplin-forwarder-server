package by.homesite.joplinforwarder.controllers.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginRequest
{
	@NotBlank
	private String username;

	@NotBlank
	@Size(min = 6, max = 40)
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

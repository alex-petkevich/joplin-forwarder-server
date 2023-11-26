package by.homesite.joplinforwarder.controllers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest
{
	@NotBlank
	private String username;

	@NotBlank
	@Size(min = 6, max = 40)
	private String password;

}

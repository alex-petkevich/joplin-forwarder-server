package by.homesite.joplinforwarder.controllers.dto.request;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest
{
	@NotBlank
	@Size(min = 3, max = 20)
	private String username;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	private Set<String> role;

	@Size(max = 50)
	private String firstname;

	@Size(max = 50)
	private String lastname;

	@Size(max = 3)
	private String lang;
}

package by.homesite.joplinforwarder.controllers.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse
{
	private String token;
	private String type = "Bearer";
	private Long id;
	private String username;
	private String email;
	private String lang;
	private List<String> roles;

	public JwtResponse(String accessToken, Long id, String username, String email, String lang, List<String> roles) {
		this.token = accessToken;
		this.id = id;
		this.username = username;
		this.email = email;
		this.roles = roles;
		this.lang = lang;
	}
}

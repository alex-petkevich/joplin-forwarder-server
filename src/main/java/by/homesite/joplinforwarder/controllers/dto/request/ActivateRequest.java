package by.homesite.joplinforwarder.controllers.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateRequest
{
	@NotBlank
	private String key;
}

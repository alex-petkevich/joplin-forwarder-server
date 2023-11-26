package by.homesite.joplinforwarder.controllers.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateRequest
{
	@NotBlank
	private String key;
}

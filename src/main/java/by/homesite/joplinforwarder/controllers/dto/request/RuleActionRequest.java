package by.homesite.joplinforwarder.controllers.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleActionRequest
{
	private Integer id;
    
	private Integer rule_id;

	private String action;

	private String action_target;

	private OffsetDateTime created_at;

	private OffsetDateTime last_modified_at;
}

package by.homesite.joplinforwarder.controllers.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleConditionRequest
{   
	private Integer id;

    private Integer rule_id;

    @NotBlank
	private String type;

	private String comparison_method;

	private Integer condition;

	private String comparison_text;

	private OffsetDateTime created_at;

	private OffsetDateTime last_modified_at;
}

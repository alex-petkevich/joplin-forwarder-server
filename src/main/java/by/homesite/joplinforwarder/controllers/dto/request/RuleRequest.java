package by.homesite.joplinforwarder.controllers.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Getter
@Setter
public class RuleRequest
{
	private Long id;

	@NotBlank
	@Size(min = 1, max = 50)
	private String name;

	@NotBlank
	@Positive
	private Integer type;

	@Positive
	private Integer comparison_method;

	@Positive
	private Integer save_in;

	private Integer final_action;

	private Integer processed;

	private String comparison_text;

	private String final_action_target;

	private OffsetDateTime created_at;

	private OffsetDateTime last_modified_at;

	private OffsetDateTime last_processed_at;
}

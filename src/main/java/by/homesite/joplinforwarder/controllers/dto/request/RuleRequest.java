package by.homesite.joplinforwarder.controllers.dto.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
	private String type;

	private String comparison_method;

	private Integer save_in;

	private String save_in_parent_id;

	private Integer processed;

	private Boolean active;

	private Boolean stop_process_rules;

	private Integer priority;

	private String final_action;

	private String comparison_text;

	private String final_action_target;

	private OffsetDateTime created_at;

	private OffsetDateTime last_modified_at;

	private OffsetDateTime last_processed_at;
}

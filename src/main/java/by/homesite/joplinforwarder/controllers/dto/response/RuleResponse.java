package by.homesite.joplinforwarder.controllers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RuleResponse {
    private Long id;

    private Integer user_id;

    private String name;

    private String type;

    private String comparison_method;

    private Integer save_in;

    private Boolean active;

    private Boolean stop_process_rules;

    private Integer priority;

    private String save_in_parent_id;

    private String final_action;

    private Integer processed;

    private String comparison_text;

    private String final_action_target;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime created_at;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime last_modified_at;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime last_processed_at;

}

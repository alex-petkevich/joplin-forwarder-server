package by.homesite.joplinforwarder.controllers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import by.homesite.joplinforwarder.model.RuleAction;
import by.homesite.joplinforwarder.model.RuleCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RuleResponse {
    private Long id;

    private Integer user_id;

    private String name;

    private Boolean save_in;

    private Boolean active;

    private Boolean stop_process_rules;

    private Integer priority;

    private String save_in_parent_id;

    private Integer processed;
    
    private List<RuleCondition> rule_conditions;
    
    private List<RuleAction> rule_actions;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime created_at;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime last_modified_at;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime last_processed_at;

}

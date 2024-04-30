package by.homesite.joplinforwarder.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rules_conditions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleCondition
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type;

    private String comparison_method;

    private String comparison_text;

    private Integer cond;

    private OffsetDateTime created_at;

    private OffsetDateTime last_modified_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    @JsonIgnore
    private Rule rule;

}

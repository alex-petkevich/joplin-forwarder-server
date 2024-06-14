package by.homesite.joplinforwarder.model;

import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "rules")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE rules SET deleted = 1 WHERE id=?")
@Where(clause = "deleted=0")
@FilterDef(name = "deletedProductFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedProductFilter", condition = "deleted = :isDeleted")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Rule implements Serializable 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(length = 50)
	private String name;

	private Boolean save_in = Boolean.TRUE;

	private Integer priority;

	private String save_in_parent_id;

	private Integer processed;

	private OffsetDateTime created_at;

	private OffsetDateTime last_modified_at;

	private OffsetDateTime last_processed_at;

	private Boolean active = Boolean.TRUE;

	private Boolean deleted = Boolean.FALSE;

	private Boolean stop_process_rules = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

    @OneToMany(mappedBy = "rule", fetch = FetchType.EAGER)
    private List<RuleCondition> ruleConditions;

    @OneToMany(mappedBy = "rule", fetch = FetchType.EAGER)
    private List<RuleAction> ruleActions;
}

package by.homesite.joplinforwarder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mails")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Mail
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String subject;

	private String text;

	private String sender;

	private String recipient;

	private Integer converted;

	private Integer processed;

	@Column(name = "added_at")
	private OffsetDateTime addedAt;

	private String attachments;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "processed_id")
	private String processedId;

	private OffsetDateTime received;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rule_id")
	@NotFound(action = NotFoundAction.IGNORE)
	private Rule rule;
}

package by.homesite.joplinforwarder.model.specification;

import org.springframework.data.jpa.domain.Specification;

import by.homesite.joplinforwarder.model.Mail;

public class MailSpecification
{
	private MailSpecification() {
		// hide creation
	}

	public static Specification<Mail> hasSubject(String subject) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.like(root.get("subject"), '%' + subject + '%');
	}
	
	public static Specification<Mail> hasText(String text) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.like(root.get("text"), '%' + text + '%');
	}

	public static Specification<Mail> hasAttachments(Boolean attachments) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.notEqual(root.get("attachments"), "");
	}

	public static Specification<Mail> hasExported(Boolean exported) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("processed"), 1);
	}

	public static Specification<Mail> hasUserId(Integer userId) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("user").get("id"), userId);
	}

}

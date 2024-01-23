package by.homesite.joplinforwarder.model.specification;

import by.homesite.joplinforwarder.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification
{
	private UserSpecification() {
		// hide creation
	}

	public static Specification<User> hasUsername(String username) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.like(root.get("username"), '%' + username + '%');
	}
	
	public static Specification<User> hasName(String name) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.or(criteriaBuilder.like(root.get("firstname"), '%' + name + '%'),
						criteriaBuilder.like(root.get("lastname"), '%' + name + '%'));
	}

	public static Specification<User> hasEmail(String email) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.like(root.get("email"), '%' + email + '%');
	}

	public static Specification<User> hasActive(Boolean active) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("active"), Boolean.TRUE.equals(active) ? 1 : 0);
	}

	public static Specification<User> hasRole(String role) {
		return (root, query, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("role_id"), role);
	}

}

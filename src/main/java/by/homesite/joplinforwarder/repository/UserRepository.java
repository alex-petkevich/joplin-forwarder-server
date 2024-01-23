package by.homesite.joplinforwarder.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.homesite.joplinforwarder.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>
{
	String USER_USERNAME_CACHE = "userUsername";

	Optional<User> findByUsername(String username);
	
	Optional<User> findByActivationKey(String activationKey);
	
	@Query(value = "SELECT u FROM User u WHERE (u.username = :key OR u.email = :key) AND u.lastModifiedAt > u.createdAt")
	Optional<User> findForResetPassword(@Param("key") String key);
	
	Boolean existsByUsername(String username);
	
	Boolean existsByEmail(String email);
}

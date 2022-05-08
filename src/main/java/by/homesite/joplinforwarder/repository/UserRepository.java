package by.homesite.joplinforwarder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.homesite.joplinforwarder.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
	Optional<User> findByUsername(String username);
	
	Optional<User> findByActivationKey(String key);
	
	@Query(value = "SELECT u FROM User u WHERE (u.username = :key OR u.email = :key) AND u.lastModifiedAt > u.createdAt AND u.active = 1")
	Optional<User> findForResetPassword(@Param("key") String key);
	
	Boolean existsByUsername(String username);
	
	Boolean existsByEmail(String email);
}

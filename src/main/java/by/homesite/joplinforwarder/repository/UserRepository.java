package by.homesite.joplinforwarder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.homesite.joplinforwarder.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
	Optional<User> findByUsername(String username);
	Optional<User> findByActivationKey(String key);
	Boolean existsByUsername(String username);
	Boolean existsByEmail(String email);
}
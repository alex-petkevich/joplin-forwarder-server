package by.homesite.joplinforwarder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.homesite.joplinforwarder.model.ERole;
import by.homesite.joplinforwarder.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>
{
	Optional<Role> findByName(ERole name);
}

package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long>
{
    List<Mail> getByUserId(Long userId);

    Mail findTop1ByUserIdOrderByReceivedDesc(Long userId);
}

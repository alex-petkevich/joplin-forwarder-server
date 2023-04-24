package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long>
{
    List<Mail> getByUserIdOrderByReceivedDesc(Long userId);

    Mail getByUserAndMessageId(User user, String messageId);

    Mail getByIdAndUserId(Integer id, Long userId);

    Mail findTop1ByUserIdOrderByReceivedDesc(Long userId);
}

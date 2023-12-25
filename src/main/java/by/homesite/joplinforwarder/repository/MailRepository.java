package by.homesite.joplinforwarder.repository;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long>, JpaSpecificationExecutor<Mail>
{
    Page<Mail> getByUserIdOrderByReceivedDesc(Integer userId, Pageable pageable);

    Mail getByUserAndMessageId(User user, String messageId);

    Mail getByIdAndUserId(Integer id, Integer userId);

    Mail findTop1ByUserIdOrderByReceivedDesc(Integer userId);
}

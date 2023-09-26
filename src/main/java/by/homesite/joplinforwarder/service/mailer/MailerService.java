package by.homesite.joplinforwarder.service.mailer;

import org.springframework.stereotype.Service;

@Service
public interface MailerService
{
	void getMail();

	void deleteOldItems(int purgeMailsPeriod);
}

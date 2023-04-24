package by.homesite.joplinforwarder.service.mailer;

public interface MailerService
{
	void getMail();

	void deleteOldItems(int purgeMailsPeriod);
}

package by.homesite.joplinforwarder.service.mailer;

public interface MailService
{
	void getMail();

	void deleteOldItems(int purgeMailsPeriod);
}

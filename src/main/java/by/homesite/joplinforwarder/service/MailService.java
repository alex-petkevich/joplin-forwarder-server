package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.MailRepository;
import by.homesite.joplinforwarder.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class MailService
{
	private final MailRepository mailRepository;

	private final ApplicationProperties applicationProperties;
	private final StorageService storageService;

	public MailService(MailRepository mailRepository, ApplicationProperties applicationProperties,@Qualifier("storageServiceStrategy") StorageService storageService) {
		this.mailRepository = mailRepository;
		this.applicationProperties = applicationProperties;
		this.storageService = storageService;
	}

	public Mail save(User user, Mail mail)
	{
		mail.setUser(user);
		return mailRepository.save(mail);
	}

	public List<Mail> getUserMails(Long userId)
	{
		return mailRepository.getByUserIdOrderByReceivedDesc(userId);
	}

	public Mail getMail(Integer id, Long userId)
	{
		return mailRepository.getByIdAndUserId(id, userId);
	}

	public void deleteMail(Integer id, Long userId)
	{
		Mail mail = mailRepository.getByIdAndUserId(id, userId);
		if (mail != null) {
			mailRepository.delete(mail);
		}
	}

	public byte[] getAttachment(String attachments, String f) throws IOException
	{
		Optional<String> fullPath = 
		Arrays.stream(attachments.split("\\|")).filter(it -> f.equals(Path.of(it).getFileName().toString())).findFirst();
		if (fullPath.isPresent()) {
			RandomAccessFile file = new RandomAccessFile(fullPath.get(), "r");
			byte[] result = new byte[(int)file.length()];
			file.readFully(result);
			return result;
		}
		return "".getBytes();
	}

	public void storeMails(User user, List<Mail> mails) {

		if (mails.isEmpty()) {
			return;
		}

		for (Mail mail: mails) {
			if (mail.getRule() != null)
			{
				String processedId = storageService.storeRecord(user, mail);

				mail.setConverted(1);
				mail.setProcessedId(processedId);

				mailRepository.save(mail);
			}
		}
	}
}

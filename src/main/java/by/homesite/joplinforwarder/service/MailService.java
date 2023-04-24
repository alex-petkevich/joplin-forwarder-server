package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.repository.MailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class MailService
{
	@Autowired
	private MailRepository mailRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

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
		String fullPath = 
		Arrays.stream(attachments.split("\\|")).filter(it -> f.equals(Path.of(it).getFileName().toString())).findFirst().toString();
		if (StringUtils.hasText(fullPath)) {
			return Files.readAllBytes(Path.of(fullPath));
		}
		return "".getBytes();
	}
}

package by.homesite.joplinforwarder.service.mapper;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sun.mail.imap.IMAPMessage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.util.BasicMapper;
import by.homesite.joplinforwarder.util.ImapUtil;

@Component
public class MailMessageMapper implements BasicMapper<Mail, IMAPMessage>
{
	private static final Logger log = LoggerFactory.getLogger(MailMessageMapper.class);

	@Override
	public IMAPMessage toEntity(Mail dto)
	{
		return null;
	}

	@Override
	public Mail toDto(IMAPMessage mess)
	{
		Mail mail = new Mail();

		try
		{
			mail.setSubject(mess.getSubject());

			mail.setReceived(mess.getReceivedDate().toInstant().atOffset(ZoneOffset.UTC));
			String recipients = Arrays.stream(mess.getAllRecipients())
					.map(javax.mail.Address::toString)
					.collect(Collectors.joining(","));
			String sender = Arrays.stream(mess.getFrom())
					.map(Address::toString)
					.collect(Collectors.joining(","));
			mail.setSender(sender);
			mail.setRecipient(recipients);
			mail.setMessageId(mess.getMessageID());
			mail.setText(ImapUtil.getTextFromMessage(mess));
		}
		catch (MessagingException | IOException e)
		{
			log.error(String.format("Can not parse message content: %s", e.getMessage()));
		}

		return null;
	}

	@Override
	public List<IMAPMessage> toEntity(List<Mail> dtoList)
	{
		return null;
	}

	@Override
	public List<Mail> toDto(List<IMAPMessage> entityList)
	{
		return null;
	}
}

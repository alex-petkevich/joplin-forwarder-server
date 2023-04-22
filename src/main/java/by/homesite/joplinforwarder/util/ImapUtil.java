package by.homesite.joplinforwarder.util;

import static by.homesite.joplinforwarder.config.Constants.CONNECT_TIMEOUT;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.util.MailSSLSocketFactory;

public class ImapUtil
{
	private static final Logger log = LoggerFactory.getLogger(ImapUtil.class);

	private ImapUtil()
	{
	}

	public static String getTextFromMessage(Message message) throws MessagingException, IOException
	{
		if (message.isMimeType("text/plain")) {
			return message.getContent().toString();
		}
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			return getTextFromMimeMultipart(mimeMultipart);
		}
		return "";
	}

	private static String getTextFromMimeMultipart(
			MimeMultipart mimeMultipart)  throws MessagingException, IOException
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				return result + "\n" + bodyPart.getContent(); // without return, same text appears twice in my tests
			}
			result.append(parseBodyPart(bodyPart));
		}
		return result.toString();
	}

	private static String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException {
		if (bodyPart.isMimeType("text/html")) {
			return bodyPart.getContent().toString();
		}
		if (bodyPart.getContent() instanceof MimeMultipart){
			return getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
		}

		return "";
	}


	public static Properties configureImapConnection()
	{

		Properties properties = new Properties();
		MailSSLSocketFactory sf;
		try
		{
			sf = new MailSSLSocketFactory();
		}
		catch (GeneralSecurityException e)
		{
			log.error("Can not get ssl certificate");
			return properties;
		}
		sf.setTrustAllHosts(true);

		properties.put("mail.imaps.ssl.socketFactory", sf);
		properties.put("mail.imaps.ssl.trust", "*");
		properties.put("mail.imaps.starttls.enable", "true");
		properties.put("mail.imaps.timeout", CONNECT_TIMEOUT);
		properties.put("mail.store.protocol", "imaps");
		return properties;
	}

}

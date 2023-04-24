package by.homesite.joplinforwarder.util;

import static by.homesite.joplinforwarder.config.Constants.CONNECT_TIMEOUT;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.sun.mail.util.MailSSLSocketFactory;

public class MailUtil
{
	private static final Logger log = LoggerFactory.getLogger(MailUtil.class);
	public static final String FILENAME_SEPARATOR = "_!_";

	private MailUtil()
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

	public static List<String> getFilesFromMessage(Message message)
	{
		try
		{
			if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				List<String> attaches = getFilesFromMimeMultipart(mimeMultipart);
				Objects.requireNonNull(attaches).removeIf(item -> item == null || "".equals(item));
				return attaches;
			}
		}
		catch (MessagingException | IOException e)
		{
			log.error(String.format("Error getting attchment: %s", e.getMessage()));
		}
		return new ArrayList<>();
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

	private static List<String> getFilesFromMimeMultipart(
			MimeMultipart mimeMultipart)  throws MessagingException, IOException
	{
		List<String> result = new ArrayList<>();
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				return Collections.emptyList();
			}
			result.addAll(parseBodyPartInFile(bodyPart));
		}
		return result;
	}

	private static String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException {
		if (bodyPart.isMimeType("text/html")) {
			return Jsoup.parse(bodyPart.getContent().toString()).text();
		} 
		if (bodyPart.getContent() instanceof MimeMultipart){
			return getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
		}

		return "";
	}

	private static List<String> parseBodyPartInFile(BodyPart bodyPart) throws MessagingException, IOException {
		if (!bodyPart.isMimeType("text/html") && StringUtils.hasText(bodyPart.getFileName())) {
			String realFileName = new String(Base64.getEncoder().encode(bodyPart.getFileName().getBytes()), StandardCharsets.UTF_8);
			File f = File.createTempFile(realFileName + FILENAME_SEPARATOR,".tmp");
			java.nio.file.Files.copy(
					bodyPart.getInputStream(),
					f.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			return Collections.singletonList(f.getAbsolutePath());
		}
		if (bodyPart.getContent() instanceof MimeMultipart){
			return getFilesFromMimeMultipart((MimeMultipart)bodyPart.getContent());
		}

		return Collections.emptyList();
	}

	public static String getRealFileName(String it)
	{
		if (it.isEmpty()) {
			return "";
		}
		String fileName = Paths.get(it).toFile().getName();
		String[] parts = fileName.split(FILENAME_SEPARATOR);

		return new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
	}
}

package by.homesite.joplinforwarder.util;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class SendMailUtil {

    private final JavaMailSender mailSender;

    private final ApplicationProperties applicationProperties;

    private final Logger log = LoggerFactory.getLogger(SendMailUtil.class);

    public SendMailUtil(
            JavaMailSender mailSender,
            ApplicationProperties applicationProperties) {
        this.mailSender = mailSender;
        this.applicationProperties = applicationProperties;
    }

    @Async
    public void send(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug(
                "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart,
                isHtml,
                to,
                subject,
                content
        );


        try {
            // Prepare message using a Spring helper
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                    message.setTo(to);
                    message.setFrom(applicationProperties.getMail().getFrom());
                    message.setSubject(subject);
                    message.setText(content, isHtml);
                }
            };
            this.mailSender.send(preparator);
            log.debug("Sent email to User '{}'", to);
        } catch (MailException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }
}

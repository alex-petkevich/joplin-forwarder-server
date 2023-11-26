package by.homesite.joplinforwarder.service;

import by.homesite.joplinforwarder.util.SendMailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.model.User;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class SendMailService {

    private final Logger log = LoggerFactory.getLogger(SendMailService.class);

    private static final String BASE_URL = "baseUrl";

    private final MailSender mailSender;

    private final SendMailUtil sendEmail;

    private final SpringTemplateEngine templateEngine;

    private final ApplicationProperties applicationProperties;

    public SendMailService(
            MailSender mailSender,
            SendMailUtil sendEmail, SpringTemplateEngine templateEngine,
            ApplicationProperties applicationProperties) {
        this.mailSender = mailSender;
        this.sendEmail = sendEmail;
        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;
    }

    public void sendEmailFromTemplate(User user, String templateName, String subject) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getUsername());
            return;
        }
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable(BASE_URL, applicationProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        sendEmail.send(user.getEmail(), subject, content, false, true);
    }

    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "Account activation");
    }

    public void sendCreationEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "Account created");
    }

    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "Password Reset");
    }

}

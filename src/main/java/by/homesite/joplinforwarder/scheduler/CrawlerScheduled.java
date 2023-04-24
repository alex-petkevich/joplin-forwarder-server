package by.homesite.joplinforwarder.scheduler;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.config.Constants;
import by.homesite.joplinforwarder.service.mailer.MailerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CrawlerScheduled {
    private static final Logger log = LoggerFactory.getLogger(CrawlerScheduled.class);
    
    private final MailerService mailerService;
    private final ApplicationProperties applicationProperties;

    public CrawlerScheduled(MailerService mailerService, ApplicationProperties applicationProperties) {
        this.mailerService = mailerService;
        this.applicationProperties = applicationProperties;
    }

    @Scheduled(fixedRate = Constants.PARSE_MAILS_PERIOD)
    public void getNewMail()
    {
        if (Constants.DISABLED.equals(applicationProperties.getGeneral().getCrawlerSchedulers()))
            return;

        log.info("Run crawler to check the new mail");
        mailerService.getMail();
    }

    @Scheduled(fixedRate = Constants.PURGE_MAILS_PERIOD)
    public void purgeOldMails() {
        if (Constants.DISABLED.equals(applicationProperties.getGeneral().getCrawlerSchedulers()))
            return;

        log.info("Clean the mail table of old items");
        mailerService.deleteOldItems(Constants.PURGE_MAILS_PERIOD);
    }
}

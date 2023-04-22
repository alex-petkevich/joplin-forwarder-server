package by.homesite.joplinforwarder.scheduler;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.config.Constants;
import by.homesite.joplinforwarder.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class CrawlerScheduled {
    private static final Logger log = LoggerFactory.getLogger(CrawlerScheduled.class);
    private final MailService mailService;
    private final ApplicationProperties applicationProperties;

    public CrawlerScheduled(MailService mailService, ApplicationProperties applicationProperties) {
        this.mailService = mailService;
        this.applicationProperties = applicationProperties;
    }

    @Scheduled(fixedRate = Constants.PARSE_MAILS_PERIOD)
    public void getNewMail()
    {
        if (Constants.DISABLED.equals(applicationProperties.getGeneral().getCrawlerSchedulers()))
            return;

        log.info("Run crawler to check the new mail");
        mailService.getMail();
    }

    @Scheduled(fixedRate = Constants.PURGE_MAILS_PERIOD)
    public void purgeOldMails() {
        if (Constants.DISABLED.equals(applicationProperties.getGeneral().getCrawlerSchedulers()))
            return;

        log.info("Clean the mail table of old items");
        mailService.deleteOldItems(Constants.PURGE_MAILS_PERIOD);
    }
}

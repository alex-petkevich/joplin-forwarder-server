package by.homesite.joplinforwarder.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@NoArgsConstructor
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final ApplicationProperties.General general = new ApplicationProperties.General();
    private final ApplicationProperties.Cors cors = new ApplicationProperties.Cors();
    private final ApplicationProperties.Mail mail = new ApplicationProperties.Mail();
    private final ApplicationProperties.Upload upload = new ApplicationProperties.Upload();

    public General getGeneral() {
        return general;
    }

    public Cors getCors() {
        return cors;
    }

    public Mail getMail() {
        return mail;
    }

    public Upload getUpload() {
        return upload;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class General {
        private String baseUrl = "baseUrl";
        private String jwtSecret = "jwtSecret";
        private String jwtExpirationMs = "jwtExpirationMs";
        private String default_lang = "default_lang";
        private String crawlerSchedulers = "crawler-schedulers";
        private String rateSchedulers = "rate-schedulers";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Cors {
        private String allowedMethods = "allowed-methods";
        private String allowedHeaders = "allowed-headers";
        private String exposedHeaders = "exposed-headers";
        private String allowCredentials = "allow-credentials";
        private String maxAge = "max-age";
        private String allowedOrigins = "allowed-origins";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Mail {
        private String from = "from";
        private String baseUrl = "baseUrl";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Upload {
        private String localPath = "local-path";
        private String uploadDir = "upload-dir";
        private String attachDir = "attach-dir";
    }
}

package by.homesite.joplinforwarder.config;

import javax.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
@EnableWebMvc

public class WebConfigurer implements ServletContextInitializer {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    private final Environment env;
    
    @Value("${joplinforwarder.cors.allowed-origins}")
    private String allowedOrigin;

    @Value("${joplinforwarder.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${joplinforwarder.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${joplinforwarder.cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${joplinforwarder.cors.allow-credentials}")
    private String allowedCredentials;

    @Value("${joplinforwarder.cors.max-age}")
    private String maxAge;

    public WebConfigurer(Environment env) {
        this.env = env;
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        if (env.getActiveProfiles().length != 0) {
            log.info("Web application configuration, using profiles: {}", (Object[]) env.getActiveProfiles());
        }

        log.info("Web application fully configured");
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(allowedOrigin);
        config.addAllowedHeader(allowedHeaders);
        config.addAllowedMethod(allowedMethods);
        config.addExposedHeader(exposedHeaders);
        config.setAllowCredentials(Boolean.parseBoolean(allowedCredentials));
        config.setMaxAge(Long.parseLong(maxAge));
        
        if (!CollectionUtils.isEmpty(config.getAllowedOrigins()) || !CollectionUtils.isEmpty(config.getAllowedOriginPatterns())) {
            log.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", config);
            source.registerCorsConfiguration("/management/**", config);
            source.registerCorsConfiguration("/v2/api-docs", config);
            source.registerCorsConfiguration("/v3/api-docs", config);
            source.registerCorsConfiguration("/swagger-resources", config);
            source.registerCorsConfiguration("/swagger-ui/**", config);
        }
        return new CorsFilter(source);
    }
}

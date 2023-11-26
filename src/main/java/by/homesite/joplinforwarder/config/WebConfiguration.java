package by.homesite.joplinforwarder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    private final ApplicationProperties applicationProperties;

    public WebConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(applicationProperties.getCors().getAllowedOrigins())
                .allowedHeaders(applicationProperties.getCors().getAllowedHeaders())
                .allowedMethods(applicationProperties.getCors().getAllowedMethods())
                .exposedHeaders(applicationProperties.getCors().getExposedHeaders())
                .allowCredentials(Boolean.parseBoolean(applicationProperties.getCors().getAllowCredentials()))
                .maxAge(Long.parseLong(applicationProperties.getCors().getMaxAge()));
    }

}

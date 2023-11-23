package by.homesite.joplinforwarder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("by.homesite.joplinforwarder.repository")
@EnableTransactionManagement
public class DatabaseConfiguration {
}

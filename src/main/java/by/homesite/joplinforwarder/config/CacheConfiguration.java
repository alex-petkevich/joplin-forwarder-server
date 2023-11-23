package by.homesite.joplinforwarder.config;

import by.homesite.joplinforwarder.repository.RoleRepository;
import by.homesite.joplinforwarder.repository.SettingsRepository;
import by.homesite.joplinforwarder.repository.UserRepository;
import by.homesite.joplinforwarder.service.FilesStorageService;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.hibernate.cache.jcache.ConfigSettings;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    private final ApplicationProperties applicationProperties;

    public CacheConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;

        ApplicationProperties.Cache cache = this.applicationProperties.getCache();

        jcacheConfiguration =
                Eh107Configuration.fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(Long.parseLong(cache.getMaxEntries())))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Long.parseLong(cache.getTimeToLiveSeconds()))))
                                .build()
                );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, UserRepository.USER_USERNAME_CACHE);
            createCache(cm, SettingsRepository.SETTINGS_EMAIL_CACHE);
            createCache(cm, RoleRepository.ROLE_BY_NAME_CACHE);
            createCache(cm, by.homesite.joplinforwarder.model.User.class.getName());
            createCache(cm, by.homesite.joplinforwarder.model.Mail.class.getName());
            createCache(cm, by.homesite.joplinforwarder.model.Rule.class.getName());
            createCache(cm, by.homesite.joplinforwarder.model.Role.class.getName());
            createCache(cm, by.homesite.joplinforwarder.model.Settings.class.getName());
            createCache(cm, FilesStorageService.LOAD_AVATAR);
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }

    @Bean
    public KeyGenerator keyGenerator() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(this.applicationProperties.getSecurity().getSeedKey().getBytes());

        keyGen.init(256, random);
        return keyGen;
    }
}

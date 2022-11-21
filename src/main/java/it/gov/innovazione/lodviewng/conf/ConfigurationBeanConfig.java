package it.gov.innovazione.lodviewng.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationBeanConfig {

    @Bean
    public ConfigurationBean conf() {
        return new ConfigurationBean("conf.ttl");
    }

    @Bean
    public ConfigurationBean confLinked() {
        return new ConfigurationBean("conf-linked.ttl");
    }
}

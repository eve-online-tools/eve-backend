package de.ronnywalter.eve.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories (basePackages = "de.ronnywalter.eve.repository")
@EnableJpaAuditing
public class JPAConfig {
}

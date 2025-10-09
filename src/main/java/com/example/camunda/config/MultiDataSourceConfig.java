package com.example.camunda.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration simplifiée avec JdbcTemplate pour éviter les conflits de configuration JPA
 */
@Configuration
public class MultiDataSourceConfig {

    /**
     * DataSource pour les mineurs (-18 ans) 
     * Base séparée uniquement pour les mineurs
     */
    @Bean
    public DataSource minorsDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:minors_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build();
    }

    /**
     * JdbcTemplate pour les adultes (utilise la base principale de Spring Boot)
     */
    @Bean
    public JdbcTemplate adultsJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * JdbcTemplate pour les mineurs (utilise la base séparée)
     */
    @Bean
    public JdbcTemplate minorsJdbcTemplate(@Qualifier("minorsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
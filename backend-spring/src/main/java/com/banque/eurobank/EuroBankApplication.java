package com.banque.eurobank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * EuroBank Core Banking Application
 * 
 * Application principale du système bancaire EuroBank.
 * Configurée pour être déployée sur WebSphere Application Server.
 * 
 * @author EuroBank IT Department
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class EuroBankApplication extends SpringBootServletInitializer {

    /**
     * Configuration pour le déploiement sur WebSphere.
     * Cette méthode est appelée par le conteneur de servlets.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EuroBankApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(EuroBankApplication.class, args);
    }
}

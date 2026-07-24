package it.simulatore.bollette.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione CORS centralizzata.
 *
 * In produzione frontend e backend condividono la stessa origine, quindi il CORS
 * e' tecnicamente superfluo. Resta configurato per lo sviluppo locale, dove Vite
 * gira su :5173 e chiama il backend su :8080.
 *
 * IMPORTANTE: non usare @CrossOrigin sui controller. L'annotazione attiva il
 * processing CORS anche per richieste same-origin, causando 403 in produzione.
 * Le origini consentite sono configurabili via APP_CORS_ALLOWED_ORIGINS.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Location")
                        .maxAge(3600);
            }
        };
    }
}

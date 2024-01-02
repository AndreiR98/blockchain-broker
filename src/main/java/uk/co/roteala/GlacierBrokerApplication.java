package uk.co.roteala;

import io.vertx.core.Vertx;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import uk.co.roteala.server.ServerInitializer;

import java.io.IOException;

@SpringBootApplication
@CrossOrigin
@PropertySource("classpath:i18n/errors.properties")
public class GlacierBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlacierBrokerApplication.class, args);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> deployVerticle(Vertx vertx, ServerInitializer verticle) {
        return event -> vertx.deployVerticle(verticle);
    }
}

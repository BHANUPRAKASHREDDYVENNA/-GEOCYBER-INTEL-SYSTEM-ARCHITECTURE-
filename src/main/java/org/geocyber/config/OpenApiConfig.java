package org.geocyber.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 / Swagger configuration for GEOCYBER INTEL API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI geocyberOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GEOCYBER INTEL API")
                        .description("Geopolitical Cyber Threat Intelligence and Multimodal Nation-State APT Attribution Platform API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GEOCYBER Research Laboratory")
                                .email("research@geocyber.org")
                                .url("https://geocyber.org"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Local Instance")
                ));
    }
}

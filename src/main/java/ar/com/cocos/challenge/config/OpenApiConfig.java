package ar.com.cocos.challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cocos Capital Challenge API")
                        .version("v1.0")
                        .description("API REST para gestión de portfolio bursátil, búsqueda de activos, y envío/cancelación de órdenes de mercado con idempotencia.")
                        .contact(new Contact()
                                .name("Cocos Challenge Team")
                                .email("dev@cocos.capital"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}

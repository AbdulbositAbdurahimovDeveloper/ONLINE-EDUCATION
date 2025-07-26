package uz.pdp.online_education.config;//package uz.pdp.online_education.config;
//
//import io.swagger.v3.oas.annotations.ExternalDocumentation;
//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
//import io.swagger.v3.oas.annotations.info.Contact;
//import io.swagger.v3.oas.annotations.info.Info;
//import io.swagger.v3.oas.annotations.info.License;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.security.SecurityScheme;
//import io.swagger.v3.oas.annotations.servers.Server;
//
import io.swagger.v3.oas.models.Components;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

///**
// Created by: Mehrojbek
// DateTime: 21/06/25 21:37
// **/
//@OpenAPIDefinition(
//        info = @Info(
//                title = "ONLINE-EDUCATION",
//                version = "${api.version}",
//                contact = @Contact(
//                        name = "Abdurahimov Abdulbosit", email = "abdulbositAbdurahimov260@gmail.com", url = "https://github.com/olx-dev-team/OLX-backend"
//                ),
//                license = @License(
//                        name = "Apache 2.0",
//                        url = "https://springdoc.org"),
//                termsOfService = "http://swagger.io/terms/",
//                description = "Spring 6 Swagger Simple Application"
//        ),
//        externalDocs = @ExternalDocumentation(
//                description = "Spring 6 Wiki Documentation", url = "https://springshop.wiki.github.org/docs"
//        ),
//        servers = {
//                @Server(
//                        url = "http://localhost:8080",
//                        description = "Production-Server"
//                ),
//                @Server(
//                        url = "http://10.10.4.147:8080",
//                        description = "Test-Server"
//                )
//
//        },
//        security = {
//                @SecurityRequirement(name = "bearerAuth"),
//                @SecurityRequirement(name = "basicAuth"),
//        }
//)
//
//@SecurityScheme(
//        name = "bearerAuth", // Xavfsizlik sxemasiga berilgan nom (yuqoridagi bilan bir xil bo'lishi kerak)
//        type = SecuritySchemeType.HTTP, // Sxema turi
//        scheme = "bearer", // Sxema nomi (JWT uchun "bearer")
//        bearerFormat = "JWT", // Token formati haqida ma'lumot (ixtiyoriy, lekin tavsiya etiladi)
//        description = "Autentifikatsiya uchun JWT tokenni 'Bearer ' prefiksi bilan kiriting. Masalan: Bearer eyJhbGciOiJI..."
//)
//@SecurityScheme(
//        name = "basicAuth",
//        type = SecuritySchemeType.HTTP,
//        scheme = "basic"
//)
//public class OpenApiConfig {
//}
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}

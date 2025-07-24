package uz.pdp.online_education.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    // application.properties'dan server URL'ini o'qib olamiz
    @Value("${server.url.development}")
    private String devUrl;

    @Value("${server.url.production}")
    private String prodUrl;

    /**
     * Bu Bean OpenAPI spetsifikatsiyasini to'liq sozlash uchun ishlatiladi.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // --- 1. Server ma'lumotlarini aniqlash ---
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Lokal development uchun server");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Production uchun server");

        // --- 2. Aloqa ma'lumotlarini aniqlash ---
        Contact contact = new Contact();
        contact.setEmail("support@online-education.com");
        contact.setName("Online Education Support");
        contact.setUrl("https://online-education.com/contact");

        // --- 3. Litsenziya ma'lumotlarini aniqlash ---
        License license = new License()
                .name("Apache License 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        // --- 4. API haqida umumiy ma'lumot (Info) ---
        Info info = new Info()
                .title("Online Education Platform API")
                .version("1.0.0")
                .description("Bu API Online Ta'lim platformasi uchun barcha endpoint'larni o'z ichiga oladi. " +
                        "U kurslarni, foydalanuvchilarni va to'lovlarni boshqarish imkonini beradi.")
                .contact(contact)
                .license(license);

        // --- 5. JWT uchun xavfsizlik sxemasi ---
        final String securitySchemeName = "bearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        // --- 6. Hamma qismlarni yig'ish ---
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer)) // Serverlar ro'yxati
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme)
                );
    }
}
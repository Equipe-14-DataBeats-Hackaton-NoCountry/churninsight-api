package com.hackathon.databeats.churninsight.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança da API ChurnInsight.
 *
 * <p>Define políticas de autenticação, autorização e CORS para a aplicação.
 * Implementa 3 cadeias de filtro separadas com diferentes regras de acesso.</p>
 *
 * <p><b>Cadeias de filtro:</b></p>
 * <ul>
 *   <li><b>Health chain:</b> Acesso público a /actuator/health (sem autenticação)</li>
 *   <li><b>Swagger chain:</b> Acesso público a /swagger-ui e /v3/api-docs</li>
 *   <li><b>Secure chain:</b> Autenticação HTTP Basic para demais endpoints</li>
 * </ul>
 *
 * <p><b>Autenticação:</b> HTTP Basic (user:password encoded em Base64).
 * Em produção, considere substituir por OAuth2/JWT.</p>
 *
 * <p><b>CORS:</b> Whitelist restritiva (configurável via property app.cors.allowed-origins).</p>
 *
 * <p><b>Autorização:</b> /cache/clear requer role ADMIN.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    @Order(1)
    public SecurityFilterChain healthFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/health", "/actuator/health/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain secureFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/cache/clear").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.realmName("ChurnInsight API"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Configuração segura de CORS - lista branca de origens
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("X-Rate-Limit-Remaining", "X-Rate-Limit-Limit"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
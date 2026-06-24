package com.evently.reserva.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/api/eventos/publicos/**").permitAll()
                        .requestMatchers("/api/eventos/buscar/**").permitAll()
                        .requestMatchers("/api/planes/**").permitAll()
                        .requestMatchers("/api/eventos/*/interno").permitAll()
                        .requestMatchers("/api/eventos/*/aforo").permitAll()
                        // Todo lo demás requiere token
                        .requestMatchers("/api/reservas/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // @Bean
    // public org.springframework.web.cors.CorsConfigurationSource
    // corsConfigurationSource() {
    // org.springframework.web.cors.CorsConfiguration config = new
    // org.springframework.web.cors.CorsConfiguration();
    // config.setAllowedOriginPatterns(java.util.List.of("*"));
    // config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE",
    // "OPTIONS"));
    // config.setAllowedHeaders(java.util.List.of("*"));
    // config.setAllowCredentials(true);

    // org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new
    // org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    // source.registerCorsConfiguration("/**", config);
    // return source;
    // }
}

package co.uceva.usuariosservice.infrastructure.config;

import co.uceva.usuariosservice.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS: Registro y Login
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/", "/api/v1/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/public-key").permitAll()

                        // 2. RUTA PÚBLICA: Refresh Token (no necesita JWT, valida el refresh token internamente)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()

                        // 3. SOLO ADMIN: Ver la lista completa de todos los usuarios
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/page/**").hasAuthority("ROLE_ADMIN")

                        // 4. CHAT: Requiere autenticación (cualquier usuario logueado)
                        .requestMatchers("/api/v1/chat/**").authenticated()

                        // 5. TODO LO DEMÁS: Requiere Token (Buscar por ID, Actualizar, Borrar, Favoritos)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Mejor que setAllowedOrigins("*") en versiones nuevas
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Añadimos Cache-Control y Pragma por si acaso para las peticiones de llaves
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "Pragma"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
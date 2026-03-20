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
@RequiredArgsConstructor // Importante para inyectar el filtro
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter; // Inyectamos tu portero

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin estado (JWT)
                .authorizeHttpRequests(auth -> auth
                        // RUTA LIBRE: El registro y el login DEBEN ser públicos
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login").permitAll()
                        // SOLO ADMIN: Ver toda la lista de usuarios
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/").hasAuthority("ROLE_ADMIN")
                        // EL RESTO: Solo requiere estar autenticado (USER o ADMIN)
                        .anyRequest().authenticated()
                )
                // AQUÍ ACTIVAMOS EL FILTRO: Revisa el token antes de dejar pasar la petición
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // ⚠️ Permitir todo para que la App móvil no sea bloqueada
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
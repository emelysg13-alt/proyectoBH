package com.backhome.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.backhome.demo.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService) {

        this.customUserDetailsService = customUserDetailsService;
    }

    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // SPRING SECURITY
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // Usar nuestro servicio personalizado
            .userDetailsService(customUserDetailsService)

            // =================================================
            // AUTORIZACIÓN
            // =================================================

            .authorizeHttpRequests(auth -> auth

                // Páginas públicas
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Administrador
                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")

                // Cliente
                .requestMatchers("/cliente/**")
                    .hasRole("CLIENTE")

                // Todo lo demás requiere autenticación
                .anyRequest()
                    .authenticated()
            )

            // =================================================
            // LOGIN
            // =================================================

            .formLogin(form -> form

                // Página que nosotros diseñamos
                .loginPage("/login")

                // Spring Security procesa este POST
                .loginProcessingUrl("/login")

                // Campo del formulario
                .usernameParameter("email")

                // Campo de contraseña
                .passwordParameter("password")

                // Login correcto
                .successHandler((request, response, authentication) -> {

                    boolean esAdmin =
                            authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                    authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                                );

                    if (esAdmin) {

                        response.sendRedirect(
                                "/admin/dashboard"
                        );

                    } else {

                        response.sendRedirect(
                                "/cliente/dashboard"
                        );
                    }
                })

                // Login incorrecto
                .failureUrl("/login?error")

                .permitAll()
            )

            // =================================================
            // LOGOUT
            // =================================================

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/login?logout")

                .invalidateHttpSession(true)

                .clearAuthentication(true)

                .deleteCookies("JSESSIONID")

                .permitAll()
            );

        return http.build();
    }
}
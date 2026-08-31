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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            .userDetailsService(customUserDetailsService)

            // =====================================================
            // AUTORIZACIONES
            // =====================================================

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")

                .requestMatchers("/cliente/**")
                    .hasRole("CLIENTE")

                .anyRequest()
                    .authenticated()
            )

            // =====================================================
            // CSRF
            // =====================================================
            //
            // Login y registro no utilizarán el token CSRF
            // porque sus formularios serán HTML normales.
            //
            // El resto de la aplicación sigue protegido.
            // =====================================================

            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/login",
                    "/registro"
                )
            )

            // =====================================================
            // LOGIN
            // =====================================================

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                .usernameParameter("email")

                .passwordParameter("password")

                .successHandler(
                    (request, response, authentication) -> {

                        boolean esAdmin =
                            authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                    authority
                                        .getAuthority()
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
                    }
                )

                .failureUrl("/login?error")

                .permitAll()
            )

            // =====================================================
            // LOGOUT
            // =====================================================

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
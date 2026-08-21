package com.backhome.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.backhome.demo.service.CustomUserDetailsService;

@Configuration
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

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                .usernameParameter("email")

                .passwordParameter("password")

                .successHandler(
                    (request, response, authentication) -> {

                        boolean esAdministrador =
                            authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(
                                    autoridad ->
                                        autoridad
                                            .getAuthority()
                                            .equals("ROLE_ADMIN")
                                );

                        if (esAdministrador) {

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

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/login?logout")

                .invalidateHttpSession(true)

                .deleteCookies("JSESSIONID")

                .permitAll()
            );

        return http.build();
    }
}
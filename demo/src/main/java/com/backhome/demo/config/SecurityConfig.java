package com.backhome.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // PÁGINAS PÚBLICAS
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                // ADMIN
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // CLIENTE
                .requestMatchers("/cliente/**")
                .hasRole("CLIENTE")

                // TODO LO DEMÁS REQUIERE LOGIN
                .anyRequest()
                .authenticated()
            )

            // LOGIN
            .formLogin(form -> form

                .loginPage("/login")

                // EL INPUT DEL FORMULARIO SE LLAMA "correo"
                .usernameParameter("correo")

                .passwordParameter("password")

                .defaultSuccessUrl("/", true)

                .permitAll()
            )

            // LOGOUT
            .logout(logout -> logout

                .logoutSuccessUrl("/login?logout")

                .permitAll()
            );

        return http.build();
    }
}
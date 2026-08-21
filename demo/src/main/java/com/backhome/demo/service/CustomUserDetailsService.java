package com.backhome.demo.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.PersonaRepository;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final PersonaRepository personaRepository;

    public CustomUserDetailsService(
            PersonaRepository personaRepository) {

        this.personaRepository = personaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Persona persona =
                personaRepository.findByCorreo(correo)
                    .orElseThrow(() ->
                        new UsernameNotFoundException(
                            "Usuario no encontrado: " + correo
                        )
                    );

        return User.builder()

                .username(persona.getEmail())

                .password(persona.getPassword())

                .roles("CLIENTE")

                .build();
    }
}
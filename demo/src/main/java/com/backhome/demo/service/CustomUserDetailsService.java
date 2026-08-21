package com.backhome.demo.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Normalizar el correo
        String emailNormalizado = email.trim().toLowerCase();

        // Buscar la persona en la base de datos
        Persona persona = personaRepository
                .findByEmail(emailNormalizado)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No existe una persona con ese email."
                        )
                );

        // ============================
        // VALIDAR ESTADO
        // ============================

        if (persona.getEstado() == null) {

            throw new UsernameNotFoundException(
                    "La cuenta no tiene un estado válido."
            );
        }

        String estado = persona.getEstado()
                .name()
                .toLowerCase();

        if (estado.equals("bloqueado")) {

            throw new UsernameNotFoundException(
                    "La cuenta se encuentra bloqueada."
            );
        }

        if (estado.equals("suspendido")) {

            throw new UsernameNotFoundException(
                    "La cuenta se encuentra suspendida."
            );
        }

        if (!estado.equals("activo")) {

            throw new UsernameNotFoundException(
                    "La cuenta no está activa."
            );
        }

        // ============================
        // COMPROBAR CLIENTE
        // ============================

        boolean esCliente =
                clienteRepository.existsByPersona_IdPersona(
                        persona.getIdPersona()
                );

        if (!esCliente) {

            throw new UsernameNotFoundException(
                    "La persona no tiene un cliente asociado."
            );
        }

        // ============================
        // CREAR USUARIO SPRING SECURITY
        // ============================

        return User.builder()
                .username(persona.getEmail())
                .password(persona.getPassword())
                .roles("CLIENTE")
                .build();
    }
}
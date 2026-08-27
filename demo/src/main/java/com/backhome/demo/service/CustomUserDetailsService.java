package com.backhome.demo.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public CustomUserDetailsService(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // =====================================================
        // 1. VALIDAR CORREO
        // =====================================================

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException(
                    "El correo es obligatorio."
            );
        }

        String emailNormalizado =
                email.trim().toLowerCase();

        // =====================================================
        // 2. BUSCAR PERSONA
        // =====================================================

        Persona persona = personaRepository
                .findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No existe una cuenta con ese correo."
                        )
                );

        // =====================================================
        // 3. VALIDAR ESTADO
        // =====================================================

        if (persona.getEstado() == null) {
            throw new UsernameNotFoundException(
                    "La cuenta no tiene un estado válido."
            );
        }

        switch (persona.getEstado()) {

            case activo:
                break;

            case bloqueado:
                throw new UsernameNotFoundException(
                        "La cuenta se encuentra bloqueada."
                );

            case suspendido:
                throw new UsernameNotFoundException(
                        "La cuenta se encuentra suspendida."
                );

            default:
                throw new UsernameNotFoundException(
                        "La cuenta no está activa."
                );
        }

        // =====================================================
        // 4. VALIDAR CONTRASEÑA
        // =====================================================

        if (persona.getPassword() == null ||
                persona.getPassword().trim().isEmpty()) {

            throw new UsernameNotFoundException(
                    "La cuenta no tiene una contraseña configurada."
            );
        }

        // =====================================================
        // 5. COMPROBAR PERFIL / ROL
        // =====================================================

        boolean esAdmin =
                administradorRepository
                        .existsByPersona_IdPersona(
                                persona.getIdPersona()
                        );

        boolean esCliente =
                clienteRepository
                        .existsByPersona_IdPersona(
                                persona.getIdPersona()
                        );

        if (!esAdmin && !esCliente) {
            throw new UsernameNotFoundException(
                    "La persona no tiene un perfil asociado."
            );
        }

        // =====================================================
        // 6. CREAR USUARIO PARA SPRING SECURITY
        // =====================================================

        User.UserBuilder userBuilder = User
                .withUsername(persona.getEmail())
                .password(persona.getPassword())
                .disabled(false)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false);

        // =====================================================
        // 7. ASIGNAR ROL
        // =====================================================

        if (esAdmin) {

            userBuilder.roles("ADMIN");

        } else {

            userBuilder.roles("CLIENTE");
        }

        return userBuilder.build();
    }
}
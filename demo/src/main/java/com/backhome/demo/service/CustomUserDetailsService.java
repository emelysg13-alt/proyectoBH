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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Normalizar el correo
        String emailNormalizado = email.trim().toLowerCase();

        // Buscar la persona en la base de datos
        Persona persona = personaRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una persona con ese email."));

        // Validar estado de la cuenta
        if (persona.getEstado() == null) {
            throw new UsernameNotFoundException("La cuenta no tiene un estado válido.");
        }

        String estado = persona.getEstado().name().toLowerCase();

        if (estado.equals("bloqueado")) {
            throw new UsernameNotFoundException("La cuenta se encuentra bloqueada.");
        }
        if (estado.equals("suspendido")) {
            throw new UsernameNotFoundException("La cuenta se encuentra suspendida.");
        }
        if (!estado.equals("activo")) {
            throw new UsernameNotFoundException("La cuenta no está activa.");
        }

        // Verificar roles y asignar de forma dinámica
        boolean esAdmin = administradorRepository.existsByPersona_IdPersona(persona.getIdPersona());
        boolean esCliente = clienteRepository.existsByPersona_IdPersona(persona.getIdPersona());

        if (!esAdmin && !esCliente) {
            throw new UsernameNotFoundException("La persona no tiene un rol o perfil asociado en el sistema.");
        }

        // Construir el usuario para Spring Security con su rol correspondiente
        var userBuilder = User.builder()
                .username(persona.getEmail())
                .password(persona.getPassword());

        if (esAdmin) {
            userBuilder.roles("ADMIN");
        } else {
            userBuilder.roles("CLIENTE");
        }

        return userBuilder.build();
    }
}
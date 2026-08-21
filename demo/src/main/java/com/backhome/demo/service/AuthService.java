package com.backhome.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Service
public class AuthService {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {
    }

    public boolean registrarCliente(
            String tipoDocumentoId,
            String numeroDocumento,
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            String email,
            String numeroTel,
            String password) {

        // Verificar si el correo ya existe
        if (personaRepository.existsByEmail(email)) {
            return false;
        }

        // Verificar si el documento ya existe
        if (personaRepository.existsByNumeroDocumento(numeroDocumento)) {
            return false;
        }

        // Crear Persona
        Persona persona = new Persona();

        persona.setTipoDocumentoId(tipoDocumentoId);
        persona.setNumeroDocumento(numeroDocumento);
        persona.setPrimerNombre(primerNombre);
        persona.setSegundoNombre(segundoNombre);
        persona.setPrimerApellido(primerApellido);
        persona.setSegundoApellido(segundoApellido);
        persona.setEmail(email);
        persona.setNumeroTel(numeroTel);

        // Guardar contraseña encriptada
        persona.setPassword(passwordEncoder.encode(password));

        // Guardar Persona
        Persona personaGuardada = personaRepository.save(persona);

        // Crear Cliente
        Cliente cliente = new Cliente();
        cliente.setPersona(personaGuardada);

        // Guardar Cliente
        clienteRepository.save(cliente);

        return true;
    }
}
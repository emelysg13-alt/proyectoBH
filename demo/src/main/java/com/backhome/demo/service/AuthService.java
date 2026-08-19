package com.backhome.demo.service;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registrarCliente(
            String nombre,
            String apellido,
            String documento,
            String telefono,
            String correo,
            String password) {

        // Verificar correo
        if (personaRepository.existsByCorreo(correo)) {
            return false;
        }

        // Verificar documento
        if (personaRepository.existsByDocumento(documento)) {
            return false;
        }

        // Crear Persona
        Persona persona = new Persona();

        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setDocumento(documento);
        persona.setTelefono(telefono);
        persona.setCorreo(correo);

        // Guardar contraseña encriptada
        persona.setPassword(passwordEncoder.encode(password));

        // Guardar persona
        Persona personaGuardada = personaRepository.save(persona);

        // Crear cliente
        Cliente cliente = new Cliente();

        cliente.setPersona(personaGuardada);

        // Guardar cliente
        clienteRepository.save(cliente);

        return true;
    }
}
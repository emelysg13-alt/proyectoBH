package com.backhome.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoPersona;
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

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registrarCliente(

            String tipoDocumentoId,
            String numeroDocumento,

            String primerNombre,
            String segundoNombre,

            String primerApellido,
            String segundoApellido,

            String email,
            String numeroTel,

            Integer estrato,

            String password) {

        // ============================
        // LIMPIAR DATOS
        // ============================

        email = email.trim().toLowerCase();

        numeroDocumento = numeroDocumento.trim();

        primerNombre = primerNombre.trim();

        primerApellido = primerApellido.trim();

        if (segundoNombre != null) {

            segundoNombre = segundoNombre.trim();

            if (segundoNombre.isEmpty()) {
                segundoNombre = null;
            }
        }

        if (segundoApellido != null) {

            segundoApellido = segundoApellido.trim();

            if (segundoApellido.isEmpty()) {
                segundoApellido = null;
            }
        }

        numeroTel = numeroTel.trim();

        // ============================
        // VALIDAR EMAIL
        // ============================

        if (personaRepository.existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con ese email."
            );
        }

        // ============================
        // VALIDAR DOCUMENTO
        // ============================

        if (personaRepository.existsByNumeroDocumento(
                numeroDocumento)) {

            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con ese documento."
            );
        }

        // ============================
        // CREAR PERSONA
        // ============================

        Persona persona = new Persona();

        persona.setTipoDocumentoId(tipoDocumentoId);

        persona.setNumeroDocumento(numeroDocumento);

        persona.setPrimerNombre(primerNombre);

        persona.setSegundoNombre(segundoNombre);

        persona.setPrimerApellido(primerApellido);

        persona.setSegundoApellido(segundoApellido);

        persona.setEmail(email);

        persona.setNumeroTel(numeroTel);


        // ============================
        // ENCRIPTAR CONTRASEÑA
        // ============================

        persona.setPassword(
                passwordEncoder.encode(password)
        );

        // ============================
        // ESTADO INICIAL
        // ============================

        persona.setEstado(
                EstadoPersona.activo
        );

        // ============================
        // GUARDAR PERSONA
        // ============================

        Persona personaGuardada =
                personaRepository.save(persona);

        // ============================
        // CREAR CLIENTE
        // ============================

        Cliente cliente = new Cliente();

        cliente.setPersona(personaGuardada);

        // ============================
        // GUARDAR CLIENTE
        // ============================

        clienteRepository.save(cliente);
    }
}
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

    /**
     * Registra una persona y posteriormente
     * la relaciona como cliente.
     */
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

        // Limpiar datos básicos
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

        // Validar email
        if (personaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con ese email."
            );
        }

        // Validar documento
        if (personaRepository.existsByNumeroDocumento(
                numeroDocumento)) {

            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con ese documento."
            );
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
        persona.setEstrato(estrato);

        /*
         * La contraseña NUNCA se guarda directamente.
         * Se guarda utilizando BCrypt.
         */
        persona.setPassword(
                passwordEncoder.encode(password)
        );

        /*
         * Tu BD tiene:
         *
         * activo
         * bloqueado
         * suspendido
         *
         * Una cuenta nueva comienza activa.
         */
        persona.setEstado(EstadoPersona.activo);

        /*
         * Primero guardamos PERSONA.
         *
         * Esto permite obtener:
         * personas.id_persona
         */
        Persona personaGuardada =
                personaRepository.save(persona);

        /*
         * Después creamos CLIENTE.
         *
         * cliente.persona_id apunta a:
         *
         * personas.id_persona
         */
        Cliente cliente = new Cliente();

        cliente.setPersona(personaGuardada);

        /*
         * NO hacemos:
         *
         * cliente.setIdCliente(
         *     personaGuardada.getIdPersona()
         * );
         *
         * porque id_cliente es AUTO_INCREMENT
         * en tu base de datos.
         */
        clienteRepository.save(cliente);
    }
}
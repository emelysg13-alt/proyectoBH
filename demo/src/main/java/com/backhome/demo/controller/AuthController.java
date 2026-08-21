package com.backhome.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Controller
public class AuthController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "auth/registro";
    }

    @PostMapping("/registro")
    @Transactional
    public String registrar(

            @RequestParam(name = "t_documento_id")
            String tipoDocumentoId,

            @RequestParam(name = "n_documento")
            String numeroDocumento,

            @RequestParam(name = "primer_nombre")
            String primerNombre,

            @RequestParam(
                name = "segundo_nombre",
                required = false
            )
            String segundoNombre,

            @RequestParam(name = "primer_apellido")
            String primerApellido,

            @RequestParam(
                name = "segundo_apellido",
                required = false
            )
            String segundoApellido,

            @RequestParam(name = "email")
            String email,

            @RequestParam(name = "numero_tel")
            String numeroTel,

            @RequestParam(
                name = "estrato",
                required = false
            )
            Integer estrato,

            @RequestParam(name = "password")
            String password,

            @RequestParam(name = "confirmPassword")
            String confirmPassword,

            RedirectAttributes redirectAttributes) {

        email = email.trim().toLowerCase();
        numeroDocumento = numeroDocumento.trim();

        if (!password.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return "redirect:/registro";
        }

        if (personaRepository.existsByEmail(email)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una cuenta con ese email."
            );

            return "redirect:/registro";
        }

        if (personaRepository.existsByNumeroDocumento(
                numeroDocumento)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una persona con ese n_documento."
            );

            return "redirect:/registro";
        }

        Persona persona = new Persona();

        persona.setTipoDocumentoId(tipoDocumentoId);
        persona.setNumeroDocumento(numeroDocumento);

        persona.setPrimerNombre(primerNombre.trim());
        persona.setSegundoNombre(
                segundoNombre == null
                        ? null
                        : segundoNombre.trim()
        );

        persona.setPrimerApellido(primerApellido.trim());
        persona.setSegundoApellido(
                segundoApellido == null
                        ? null
                        : segundoApellido.trim()
        );

        persona.setEmail(email);
        persona.setNumeroTel(numeroTel.trim());
        persona.setEstrato(estrato);

        persona.setPassword(
                passwordEncoder.encode(password)
        );

        persona.setEstado(EstadoPersona.activo);

        Persona personaGuardada =
                personaRepository.save(persona);

        Cliente cliente = new Cliente();

        cliente.setPersona(personaGuardada);

        clienteRepository.save(cliente);

        redirectAttributes.addFlashAttribute(
                "success",
                "Cuenta creada correctamente. "
                + "Ahora puedes iniciar sesión."
        );

        return "redirect:/login";
    }
}
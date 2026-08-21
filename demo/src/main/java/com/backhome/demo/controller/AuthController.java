package com.backhome.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Controller
public class AuthController {

    private static final String REDIRECT_REGISTRO = "redirect:/registro";
    private static final String REDIRECT_LOGIN = "redirect:/login";

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
    public String registrar(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String documento,
            @RequestParam(required = false) String telefono,
            @RequestParam String correo,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return REDIRECT_REGISTRO;
        }

        if (personaRepository.existsByCorreo(correo)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una cuenta con ese correo."
            );

            return REDIRECT_REGISTRO;
        }

        if (personaRepository.existsByDocumento(documento)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una persona con ese documento."
            );

            return REDIRECT_REGISTRO;
        }

        Persona persona = new Persona();

        persona.setPrimerNombre(nombre);
        persona.setPrimerApellido(apellido);
        persona.setNumeroDocumento(documento);
        persona.setNumeroTel(telefono);
        persona.setEmail(correo);

        persona.setPassword(
                passwordEncoder.encode(password)
        );

        Persona personaGuardada =
                personaRepository.save(persona);

        Cliente cliente = new Cliente();

        cliente.setPersona(personaGuardada);
        cliente.setIdCliente(personaGuardada.getIdPersona());

        clienteRepository.save(cliente);

        redirectAttributes.addFlashAttribute(
                "success",
                "¡Cuenta creada correctamente! Ahora puedes iniciar sesión."
        );

        return REDIRECT_LOGIN;
    }
}
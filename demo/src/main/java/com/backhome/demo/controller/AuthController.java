package com.backhome.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.dto.RegistroForm;
import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.TipoDocumentoRepository;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            PasswordEncoder passwordEncoder) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    // =====================================================
    // MOSTRAR REGISTRO
    // =====================================================

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {

        if (!model.containsAttribute("registroForm")) {
            model.addAttribute(
                    "registroForm",
                    new RegistroForm()
            );
        }

        model.addAttribute(
                "tiposDocumento",
                tipoDocumentoRepository.findAll()
        );

        return "auth/registro";
    }

    // =====================================================
    // PROCESAR REGISTRO
    // =====================================================

    @PostMapping("/registro")
    @Transactional
    public String registrar(
            @Valid @ModelAttribute("registroForm") RegistroForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // =================================================
        // 1. VALIDACIONES DEL FORMULARIO
        // =================================================

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "tiposDocumento",
                    tipoDocumentoRepository.findAll()
            );

            return "auth/registro";
        }

        // =================================================
        // 2. NORMALIZAR DATOS
        // =================================================

        String email = form.getEmail()
                .trim()
                .toLowerCase();

        String numeroDocumento = form.getNumeroDocumento()
                .trim();

        // =================================================
        // 3. CONFIRMAR CONTRASEÑA
        // =================================================

        if (!form.getPassword()
                .equals(form.getConfirmPassword())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            redirectAttributes.addFlashAttribute(
                    "registroForm",
                    form
            );

            return "redirect:/registro";
        }

        // =================================================
        // 4. COMPROBAR EMAIL
        // =================================================

        if (personaRepository
                .existsByEmailIgnoreCase(email)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una cuenta con ese correo."
            );

            redirectAttributes.addFlashAttribute(
                    "registroForm",
                    form
            );

            return "redirect:/registro";
        }

        // =================================================
        // 5. COMPROBAR DOCUMENTO
        // =================================================

        if (personaRepository
                .existsByNumeroDocumento(numeroDocumento)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una persona con ese número de documento."
            );

            redirectAttributes.addFlashAttribute(
                    "registroForm",
                    form
            );

            return "redirect:/registro";
        }

        // =================================================
        // 6. CREAR PERSONA
        // =================================================

        Persona persona = new Persona();

        persona.setTipoDocumentoId(
                form.getTipoDocumentoId()
        );

        persona.setNumeroDocumento(
                numeroDocumento
        );

        persona.setPrimerNombre(
                form.getPrimerNombre().trim()
        );

        persona.setSegundoNombre(
                form.getSegundoNombre() == null ||
                form.getSegundoNombre().trim().isEmpty()
                        ? null
                        : form.getSegundoNombre().trim()
        );

        persona.setPrimerApellido(
                form.getPrimerApellido().trim()
        );

        persona.setSegundoApellido(
                form.getSegundoApellido() == null ||
                form.getSegundoApellido().trim().isEmpty()
                        ? null
                        : form.getSegundoApellido().trim()
        );

        persona.setEmail(email);

        persona.setNumeroTel(
                form.getNumeroTel().trim()
        );

        // =================================================
        // 7. ENCRIPTAR CONTRASEÑA
        // =================================================

        persona.setPassword(
                passwordEncoder.encode(
                        form.getPassword()
                )
        );

        // =================================================
        // 8. ESTADO INICIAL
        // =================================================

        persona.setEstado(
                EstadoPersona.activo
        );

        // =================================================
        // 9. GUARDAR PERSONA
        // =================================================

        Persona personaGuardada =
                personaRepository.save(persona);

        // =================================================
        // 10. CREAR CLIENTE
        // =================================================

        Cliente cliente = new Cliente();

        cliente.setPersona(personaGuardada);

        clienteRepository.save(cliente);

        // =================================================
        // 11. MENSAJE DE ÉXITO
        // =================================================

        redirectAttributes.addFlashAttribute(
                "success",
                "Cuenta creada correctamente. Ahora puedes iniciar sesión."
        );

        return "redirect:/login";
    }
}
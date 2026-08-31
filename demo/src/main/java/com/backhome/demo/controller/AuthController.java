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

import jakarta.servlet.http.HttpServletResponse;
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
    public String mostrarLogin(
            HttpServletResponse response) {

        /*
         * Evitamos que el navegador conserve una versión
         * antigua o incompleta de la página de login.
         *
         * Esto es especialmente importante después de
         * cerrar sesión y volver a /login.
         */

        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );

        return "auth/login";
    }


    // =====================================================
    // REGISTRO
    // =====================================================

    @GetMapping("/registro")
    public String mostrarRegistro(
            Model model) {

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
    // PROCESAR REGISTRO - DATO STACK
    // =====================================================

    @PostMapping("/registro")
    @Transactional
    public String registrar(
            @Valid @ModelAttribute("registroForm") RegistroForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {


        // -------------------------------------------------
        // VALIDACIONES
        // -------------------------------------------------

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "tiposDocumento",
                    tipoDocumentoRepository.findAll()
            );

            return "auth/registro";
        }


        // -------------------------------------------------
        // VALIDAR CONFIRMACIÓN DE CONTRASEÑA
        // -------------------------------------------------

        if (form.getPassword() == null ||
                form.getConfirmPassword() == null ||
                !form.getPassword().equals(
                        form.getConfirmPassword())) {

            model.addAttribute(
                    "tiposDocumento",
                    tipoDocumentoRepository.findAll()
            );

            model.addAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return "auth/registro";
        }


        // -------------------------------------------------
        // NORMALIZAR EMAIL
        // -------------------------------------------------

        String email = form.getEmail()
                .trim()
                .toLowerCase();


        // -------------------------------------------------
        // NORMALIZAR DOCUMENTO
        // -------------------------------------------------

        String numeroDocumento =
                form.getNumeroDocumento()
                        .trim();


        // -------------------------------------------------
        // COMPROBAR EMAIL
        // -------------------------------------------------

        if (personaRepository.existsByEmailIgnoreCase(email)) {

            model.addAttribute(
                    "tiposDocumento",
                    tipoDocumentoRepository.findAll()
            );

            model.addAttribute(
                    "error",
                    "Ya existe una cuenta registrada con ese correo."
            );

            return "auth/registro";
        }


        // -------------------------------------------------
        // COMPROBAR DOCUMENTO
        // -------------------------------------------------

        if (personaRepository.existsByNumeroDocumento(
                numeroDocumento)) {

            model.addAttribute(
                    "tiposDocumento",
                    tipoDocumentoRepository.findAll()
            );

            model.addAttribute(
                    "error",
                    "Ya existe una persona registrada con ese número de documento."
            );

            return "auth/registro";
        }


        // -------------------------------------------------
        // CREAR PERSONA
        // -------------------------------------------------

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


        // -------------------------------------------------
        // SEGUNDO NOMBRE
        // -------------------------------------------------

        if (form.getSegundoNombre() != null &&
                !form.getSegundoNombre()
                        .trim()
                        .isEmpty()) {

            persona.setSegundoNombre(
                    form.getSegundoNombre().trim()
            );

        } else {

            persona.setSegundoNombre(null);
        }


        // -------------------------------------------------
        // PRIMER APELLIDO
        // -------------------------------------------------

        persona.setPrimerApellido(
                form.getPrimerApellido().trim()
        );


        // -------------------------------------------------
        // SEGUNDO APELLIDO
        // -------------------------------------------------

        if (form.getSegundoApellido() != null &&
                !form.getSegundoApellido()
                        .trim()
                        .isEmpty()) {

            persona.setSegundoApellido(
                    form.getSegundoApellido().trim()
            );

        } else {

            persona.setSegundoApellido(null);
        }


        // -------------------------------------------------
        // EMAIL
        // -------------------------------------------------

        persona.setEmail(email);


        // -------------------------------------------------
        // TELÉFONO
        // -------------------------------------------------

        persona.setNumeroTel(
                form.getNumeroTel().trim()
        );


        // -------------------------------------------------
        // CONTRASEÑA
        // -------------------------------------------------

        persona.setPassword(
                passwordEncoder.encode(
                        form.getPassword()
                )
        );


        // -------------------------------------------------
        // ESTADO
        // -------------------------------------------------

        persona.setEstado(
                EstadoPersona.activo
        );


        // -------------------------------------------------
        // GUARDAR PERSONA
        // -------------------------------------------------

        Persona personaGuardada =
                personaRepository.save(persona);


        // -------------------------------------------------
        // CREAR CLIENTE
        // -------------------------------------------------

        Cliente cliente = new Cliente();

        cliente.setPersona(
                personaGuardada
        );


        // -------------------------------------------------
        // GUARDAR CLIENTE
        // -------------------------------------------------

        clienteRepository.save(cliente);


        // -------------------------------------------------
        // REGISTRO EXITOSO
        // -------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "success",
                "Cuenta creada correctamente. Ahora puedes iniciar sesión."
        );


        return "redirect:/login";
    }
}
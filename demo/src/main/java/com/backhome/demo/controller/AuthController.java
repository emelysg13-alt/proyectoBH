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

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        // Pasamos un objeto vacío 'registroForm' para que Thymeleaf pueda enlazarlo con el formulario
        if (!model.containsAttribute("registroForm")) {
            model.addAttribute("registroForm", new RegistroForm());
        }
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAll());
        return "auth/registro";
    }

    @PostMapping("/registro")
    @Transactional
    public String registrar(
            @Valid @ModelAttribute("registroForm") RegistroForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1. Si hay errores de validación en las anotaciones del DTO (campos vacíos, email inválido, etc.)
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAll());
            return "auth/registro";
        }

        // 2. Validar que las contraseñas coincidan
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            redirectAttributes.addFlashAttribute("registroForm", form);
            return "redirect:/registro";
        }

        String emailLimpio = form.getEmail().trim().toLowerCase();
        String docLimpio = form.getNumeroDocumento().trim();

        // 3. Validar si el correo ya existe
        if (personaRepository.existsByEmail(emailLimpio)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe una cuenta con ese email.");
            redirectAttributes.addFlashAttribute("registroForm", form);
            return "redirect:/registro";
        }

        // 4. Validar si el número de documento ya existe
        if (personaRepository.existsByNumeroDocumento(docLimpio)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe una persona con ese número de documento.");
            redirectAttributes.addFlashAttribute("registroForm", form);
            return "redirect:/registro";
        }

        // 5. Crear y guardar la Persona
        Persona persona = new Persona();
        persona.setTipoDocumentoId(form.getTipoDocumentoId());
        persona.setNumeroDocumento(docLimpio);
        persona.setPrimerNombre(form.getPrimerNombre().trim());
        persona.setSegundoNombre(form.getSegundoNombre() == null ? null : form.getSegundoNombre().trim());
        persona.setPrimerApellido(form.getPrimerApellido().trim());
        persona.setSegundoApellido(form.getSegundoApellido() == null ? null : form.getSegundoApellido().trim());
        persona.setEmail(emailLimpio);
        persona.setNumeroTel(form.getNumeroTel().trim());
        persona.setPassword(passwordEncoder.encode(form.getPassword()));
        persona.setEstado(EstadoPersona.activo);

        Persona personaGuardada = personaRepository.save(persona);

        // 6. Crear y guardar el Cliente asociado
        Cliente cliente = new Cliente();
        cliente.setPersona(personaGuardada);
        clienteRepository.save(cliente);

        redirectAttributes.addFlashAttribute("success", "Cuenta creada correctamente. Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }
}
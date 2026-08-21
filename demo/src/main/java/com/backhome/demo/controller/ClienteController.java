package com.backhome.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.PersonaRepository;

@Controller
public class ClienteController {

    private final PersonaRepository personaRepository;

    public ClienteController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @GetMapping("/cliente/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Persona persona = personaRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        model.addAttribute("persona", persona);

        return "cliente/dashboard";
    }
}
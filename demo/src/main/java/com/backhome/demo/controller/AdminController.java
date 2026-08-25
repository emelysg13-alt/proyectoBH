package com.backhome.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Controller
public class AdminController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public AdminController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el administrador."
                        )
                );

        // Información del administrador
        model.addAttribute("persona", persona);

        // Estadísticas generales
        model.addAttribute(
                "totalUsuarios",
                personaRepository.count()
        );

        model.addAttribute(
                "totalClientes",
                clienteRepository.count()
        );

        model.addAttribute(
                "totalAdministradores",
                administradorRepository.count()
        );

        // Estados de usuarios
        model.addAttribute(
                "usuariosActivos",
                personaRepository.countByEstado(
                        EstadoPersona.activo
                )
        );

        model.addAttribute(
                "usuariosBloqueados",
                personaRepository.countByEstado(
                        EstadoPersona.bloqueado
                )
        );

        model.addAttribute(
                "usuariosSuspendidos",
                personaRepository.countByEstado(
                        EstadoPersona.suspendido
                )
        );

        return "admin/dashboard";
    }
}
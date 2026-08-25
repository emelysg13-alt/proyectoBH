package com.backhome.demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.TipoDocumento;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.TipoDocumentoRepository;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            PasswordEncoder passwordEncoder) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // LISTAR USUARIOS
    // =====================================================

    @GetMapping
    public String listarUsuarios(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol,
            Model model) {

        List<Persona> usuarios = personaRepository.findAll();

        Map<Integer, String> roles = construirMapaRoles();

        String textoBusqueda =
                buscar == null ? "" : buscar.trim().toLowerCase();

        List<Persona> filtrados = new ArrayList<>();

        for (Persona persona : usuarios) {

            String nombreCompleto =
                    ((persona.getPrimerNombre() == null ? "" : persona.getPrimerNombre()) + " "
                    + (persona.getSegundoNombre() == null ? "" : persona.getSegundoNombre()) + " "
                    + (persona.getPrimerApellido() == null ? "" : persona.getPrimerApellido()) + " "
                    + (persona.getSegundoApellido() == null ? "" : persona.getSegundoApellido()))
                    .trim()
                    .toLowerCase();

            String documento =
                    persona.getNumeroDocumento() == null
                            ? ""
                            : persona.getNumeroDocumento().toLowerCase();

            String email =
                    persona.getEmail() == null
                            ? ""
                            : persona.getEmail().toLowerCase();

            String telefono =
                    persona.getNumeroTel() == null
                            ? ""
                            : persona.getNumeroTel().toLowerCase();

            boolean coincideBusqueda =
                    textoBusqueda.isEmpty()
                    || nombreCompleto.contains(textoBusqueda)
                    || documento.contains(textoBusqueda)
                    || email.contains(textoBusqueda)
                    || telefono.contains(textoBusqueda);

            boolean coincideEstado =
                    estado == null
                    || estado.isBlank()
                    || persona.getEstado() == null
                    || persona.getEstado().name().equalsIgnoreCase(estado);

            String rolPersona =
                    roles.getOrDefault(
                            persona.getIdPersona(),
                            "Sin rol"
                    );

            boolean coincideRol =
                    rol == null
                    || rol.isBlank()
                    || rol.equalsIgnoreCase(rolPersona);

            if (coincideBusqueda
                    && coincideEstado
                    && coincideRol) {

                filtrados.add(persona);
            }
        }

        filtrados.sort(
                Comparator.comparing(
                        Persona::getPrimerNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                )
        );

        model.addAttribute("usuarios", filtrados);
        model.addAttribute("roles", roles);

        model.addAttribute(
                "totalUsuarios",
                usuarios.size()
        );

        model.addAttribute(
                "totalActivos",
                usuarios.stream()
                        .filter(p ->
                                p.getEstado() == EstadoPersona.activo)
                        .count()
        );

        model.addAttribute(
                "totalBloqueados",
                usuarios.stream()
                        .filter(p ->
                                p.getEstado() == EstadoPersona.bloqueado)
                        .count()
        );

        model.addAttribute(
                "totalSuspendidos",
                usuarios.stream()
                        .filter(p ->
                                p.getEstado() == EstadoPersona.suspendido)
                        .count()
        );

        model.addAttribute(
                "buscar",
                buscar == null ? "" : buscar
        );

        model.addAttribute(
                "estadoSeleccionado",
                estado == null ? "" : estado
        );

        model.addAttribute(
                "rolSeleccionado",
                rol == null ? "" : rol
        );

        return "admin/usuarios/lista";
    }

    // =====================================================
    // VER DETALLE
    // =====================================================

    @GetMapping("/{id}")
    public String verUsuario(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Persona usuario = personaRepository
                .findById(id)
                .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        String rol = obtenerRol(usuario);

        String nombreTipoDocumento =
                tipoDocumentoRepository
                        .findById(usuario.getTipoDocumentoId())
                        .map(TipoDocumento::getNDoc)
                        .orElse(usuario.getTipoDocumentoId());

        model.addAttribute("usuario", usuario);
        model.addAttribute("rol", rol);
        model.addAttribute(
                "nombreTipoDocumento",
                nombreTipoDocumento
        );

        return "admin/usuarios/detalle";
    }

    // =====================================================
    // FORMULARIO EDITAR
    // =====================================================

    @GetMapping("/{id}/editar")
    public String formularioEditar(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Persona usuario = personaRepository
                .findById(id)
                .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        model.addAttribute("usuario", usuario);

        model.addAttribute(
                "tiposDocumento",
                tipoDocumentoRepository.findAll()
        );

        model.addAttribute(
                "rol",
                obtenerRol(usuario)
        );

        return "admin/usuarios/editar";
    }

    // =====================================================
    // GUARDAR EDICIÓN
    // =====================================================

    @PostMapping("/{id}/editar")
    public String editarUsuario(
            @PathVariable Integer id,
            @RequestParam String tipoDocumentoId,
            @RequestParam String numeroDocumento,
            @RequestParam String primerNombre,
            @RequestParam(required = false) String segundoNombre,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam String email,
            @RequestParam String numeroTel,
            @RequestParam(required = false) Integer estrato,
            RedirectAttributes redirectAttributes) {

        Persona usuario = personaRepository
                .findById(id)
                .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        numeroDocumento = numeroDocumento.trim();
        email = email.trim().toLowerCase();

        boolean documentoDuplicado =
                personaRepository.existsByNumeroDocumento(
                        numeroDocumento
                )
                && !numeroDocumento.equals(
                        usuario.getNumeroDocumento()
                );

        if (documentoDuplicado) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ese número de documento ya pertenece a otro usuario."
            );

            return "redirect:/admin/usuarios/" + id + "/editar";
        }

        boolean correoDuplicado =
                personaRepository.existsByEmailIgnoreCase(email)
                && !email.equalsIgnoreCase(
                        usuario.getEmail()
                );

        if (correoDuplicado) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ese correo ya pertenece a otro usuario."
            );

            return "redirect:/admin/usuarios/" + id + "/editar";
        }

        if (!tipoDocumentoRepository.existsById(tipoDocumentoId)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El tipo de documento seleccionado no existe."
            );

            return "redirect:/admin/usuarios/" + id + "/editar";
        }

        usuario.setTipoDocumentoId(tipoDocumentoId);
        usuario.setNumeroDocumento(numeroDocumento);
        usuario.setPrimerNombre(primerNombre.trim());
        usuario.setSegundoNombre(
                segundoNombre == null
                        ? null
                        : segundoNombre.trim()
        );
        usuario.setPrimerApellido(primerApellido.trim());
        usuario.setSegundoApellido(
                segundoApellido == null
                        ? null
                        : segundoApellido.trim()
        );
        usuario.setEmail(email);
        usuario.setNumeroTel(numeroTel.trim());
        usuario.setEstrato(estrato);

        personaRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "La información del usuario fue actualizada correctamente."
        );

        return "redirect:/admin/usuarios/" + id;
    }

    // =====================================================
    // CAMBIAR ESTADO
    // =====================================================

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Persona usuario = personaRepository
                .findById(id)
                .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        // No permitir que el administrador bloquee
        // o suspenda su propia cuenta.

        if (authentication != null
                && authentication.getName() != null
                && usuario.getEmail() != null
                && usuario.getEmail()
                        .equalsIgnoreCase(authentication.getName())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No puedes bloquear o suspender la cuenta con la que estás administrando el sistema."
            );

            return "redirect:/admin/usuarios/" + id;
        }

        EstadoPersona estado;

        try {

            estado = EstadoPersona.valueOf(
                    nuevoEstado.toLowerCase()
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado seleccionado no es válido."
            );

            return "redirect:/admin/usuarios/" + id;
        }

        usuario.setEstado(estado);

        personaRepository.save(usuario);

        String mensaje;

        switch (estado) {

            case activo:
                mensaje = "El usuario fue activado correctamente.";
                break;

            case bloqueado:
                mensaje = "El usuario fue bloqueado correctamente.";
                break;

            case suspendido:
                mensaje = "El usuario fue suspendido correctamente.";
                break;

            default:
                mensaje = "Estado actualizado.";
        }

        redirectAttributes.addFlashAttribute(
                "success",
                mensaje
        );

        return "redirect:/admin/usuarios/" + id;
    }

    // =====================================================
    // CAMBIAR CONTRASEÑA
    // =====================================================

    @PostMapping("/{id}/password")
    public String cambiarPassword(
            @PathVariable Integer id,
            @RequestParam String nuevaPassword,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        Persona usuario = personaRepository
                .findById(id)
                .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        if (nuevaPassword == null
                || nuevaPassword.length() < 6) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La contraseña debe tener mínimo 6 caracteres."
            );

            return "redirect:/admin/usuarios/" + id;
        }

        if (!nuevaPassword.equals(confirmarPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return "redirect:/admin/usuarios/" + id;
        }

        usuario.setPassword(
                passwordEncoder.encode(nuevaPassword)
        );

        personaRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "La contraseña fue actualizada correctamente."
        );

        return "redirect:/admin/usuarios/" + id;
    }

    // =====================================================
    // MAPA DE ROLES
    // =====================================================

    private Map<Integer, String> construirMapaRoles() {

        Map<Integer, String> roles = new HashMap<>();

        administradorRepository.findAll()
                .forEach(admin -> {

                    if (admin.getPersona() != null) {

                        roles.put(
                                admin.getPersona().getIdPersona(),
                                "Administrador"
                        );
                    }
                });

        clienteRepository.findAll()
                .forEach(cliente -> {

                    if (cliente.getPersona() != null
                            && !roles.containsKey(
                                    cliente.getPersona().getIdPersona())) {

                        roles.put(
                                cliente.getPersona().getIdPersona(),
                                "Cliente"
                        );
                    }
                });

        return roles;
    }

    private String obtenerRol(Persona persona) {

        if (persona == null) {
            return "Sin rol";
        }

        if (administradorRepository
                .existsByPersona_IdPersona(
                        persona.getIdPersona())) {

            return "Administrador";
        }

        if (clienteRepository
                .existsByPersona_IdPersona(
                        persona.getIdPersona())) {

            return "Cliente";
        }

        return "Sin rol";
    }
}
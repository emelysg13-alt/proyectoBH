package com.backhome.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    Optional<Persona> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);

    long countByEstado(EstadoPersona estado);

    List<Persona> findAllByOrderByIdPersonaDesc();

    List<Persona> findByEstadoOrderByIdPersonaDesc(
            EstadoPersona estado
    );
}
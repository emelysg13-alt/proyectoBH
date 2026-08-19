package com.backhome.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backhome.demo.model.Persona;

public interface PersonaRepository
        extends JpaRepository<Persona, Integer> {

    Optional<Persona> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByDocumento(String documento);
}
package com.backhome.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backhome.demo.model.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    @Query("SELECT p FROM Persona p WHERE p.email = :correo")
    Optional<Persona> findByCorreo(@Param("correo") String correo);

    @Query("SELECT COUNT(p) > 0 FROM Persona p WHERE p.email = :correo")
    boolean existsByCorreo(@Param("correo") String correo);

    @Query("SELECT COUNT(p) > 0 FROM Persona p WHERE p.numeroDocumento = :documento")
    boolean existsByDocumento(@Param("documento") String documento);
}
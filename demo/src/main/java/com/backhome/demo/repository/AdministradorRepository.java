package com.backhome.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.backhome.demo.model.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    
    // Método necesario para verificar si una persona es administrador según tu base de datos
    boolean existsByPersona_IdPersona(Integer idPersona);
}
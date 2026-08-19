package com.backhome.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backhome.demo.model.Cliente;

public interface ClienteRepository
        extends JpaRepository<Cliente, Integer> {

}
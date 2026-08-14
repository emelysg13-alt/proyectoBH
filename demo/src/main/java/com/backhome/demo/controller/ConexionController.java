
package com.backhome.demo.controller;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConexionController {

    private final DataSource dataSource;

    public ConexionController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/conexion")
    public String comprobarConexion() {

        try (Connection conexion = dataSource.getConnection()) {

            return "✅ CONEXIÓN EXITOSA A LA BASE DE DATOS<br>" +
                   "Base de datos: " + conexion.getCatalog() + "<br>" +
                   "Servidor: " + conexion.getMetaData().getURL();

        } catch (Exception e) {

            return "❌ ERROR DE CONEXIÓN: " + e.getMessage();
        }
    }
}

package com.backhome.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.backhome.demo.model.TipoDocumento;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, String> {
}
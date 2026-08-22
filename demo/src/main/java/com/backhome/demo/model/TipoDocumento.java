package com.backhome.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {

    @Id
    @Column(name = "id_t_doc")
    private String idTDoc;

    @Column(name = "n_doc", nullable = false)
    private String nDoc;

    public TipoDocumento() {
    }

    public String getIdTDoc() {
        return idTDoc;
    }

    public void setIdTDoc(String idTDoc) {
        this.idTDoc = idTDoc;
    }

    public String getNDoc() {
        return nDoc;
    }

    public void setNDoc(String nDoc) {
        this.nDoc = nDoc;
    }
}
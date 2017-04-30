/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.encens.khipus.model.customers;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Diego
 */
@Entity
@Table(name = "territoriotrabajo")
public class Territoriotrabajo implements Serializable {

    @Id
    @Column(name = "IDTERRITORIOTRABAJO")
    private Long idterritoriotrabajo;
    @Column(name = "NOMBRE")
    private String nombre;
    @Column(name = "PAIS")
    private String pais;
    @Column(name = "DESCRIPCION")
    private String descripcion;
    @OneToMany(mappedBy = "territoriotrabajo")
    private Collection<Client> clientes;
    @JoinColumn(name = "IDDISTRIBUIDOR", referencedColumnName = "IDPERSONACLIENTE")
    @ManyToOne
    private Client distribuidor;

    public Territoriotrabajo() {
    }

    public Territoriotrabajo(Long idterritoriotrabajo) {
        this.idterritoriotrabajo = idterritoriotrabajo;
    }

    public Long getIdterritoriotrabajo() {
        return idterritoriotrabajo;
    }

    public void setIdterritoriotrabajo(Long idterritoriotrabajo) {
        this.idterritoriotrabajo = idterritoriotrabajo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        if(this.pais == null)
            pais = "Bolivia";
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idterritoriotrabajo != null ? idterritoriotrabajo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Territoriotrabajo)) {
            return false;
        }
        Territoriotrabajo other = (Territoriotrabajo) object;
        if ((this.idterritoriotrabajo == null && other.idterritoriotrabajo != null) || (this.idterritoriotrabajo != null && !this.idterritoriotrabajo.equals(other.idterritoriotrabajo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return nombre;
    }

}

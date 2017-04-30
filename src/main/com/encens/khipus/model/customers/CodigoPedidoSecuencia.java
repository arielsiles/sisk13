package com.encens.khipus.model.customers;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Diego on 23/03/2015.
 */
@Entity
@Table(name = "codigopedidosecuencia")
public class CodigoPedidoSecuencia implements Serializable {
    @Id
    @Column(name = "secuencia")
    private Long secuencia;

    public Long getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(Long secuencia) {
        this.secuencia = secuencia;
    }
}

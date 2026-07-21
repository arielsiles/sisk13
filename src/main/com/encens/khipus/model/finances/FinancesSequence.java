package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Correlativos de finanzas de la tabla '_sequence' (seq_name / seq_val), POR COMPANIA.
 * Es la misma tabla que hoy usan las funciones almacenadas getNextSeq(), evolucionada
 * al estilo de la entidad Sequence (gensecuencia): con compania (idcompania) y control
 * de concurrencia optimista (version).
 *
 * Se usa para generar los correlativos de asientos, vales y ventas desde Hibernate,
 * con la compania real de la sesion, sin la condicion de carrera de la funcion.
 * La clave natural es (seq_name, idcompania) -> FinancesSequenceId.
 *
 * @author
 * @version 1.0
 */
@Entity
@Table(name = "_sequence", schema = Constants.FINANCES_SCHEMA)
public class FinancesSequence implements BaseModel {

    @EmbeddedId
    private FinancesSequenceId id = new FinancesSequenceId();

    @Column(name = "seq_val", nullable = false)
    private long value;

    @Version
    @Column(name = "version")
    private Long version;

    public FinancesSequence() {
    }

    public FinancesSequence(String name, Long companyId, long value) {
        this.id = new FinancesSequenceId(name, companyId);
        this.value = value;
    }

    public Object getId() {
        return id;
    }

    public void setId(FinancesSequenceId id) {
        this.id = id;
    }

    public String getName() {
        return id != null ? id.getName() : null;
    }

    public Long getCompanyId() {
        return id != null ? id.getCompanyId() : null;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

package com.encens.khipus.model.finances;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Clave compuesta de FinancesSequence: nombre de la secuencia + compania.
 * Permite tener el mismo correlativo (p.ej. 'ASIENTO') independiente por compania.
 *
 * @author
 * @version 1.0
 */
@Embeddable
public class FinancesSequenceId implements Serializable {

    @Column(name = "seq_name", length = 50)
    private String name;

    @Column(name = "idcompania")
    private Long companyId;

    public FinancesSequenceId() {
    }

    public FinancesSequenceId(String name, Long companyId) {
        this.name = name;
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FinancesSequenceId that = (FinancesSequenceId) o;
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return companyId != null ? companyId.equals(that.companyId) : that.companyId == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (companyId != null ? companyId.hashCode() : 0);
        return result;
    }
}

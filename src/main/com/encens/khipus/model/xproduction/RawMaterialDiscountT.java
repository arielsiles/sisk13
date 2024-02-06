package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.xproduction.RawMaterialDiscountType;
import com.encens.khipus.util.Constants;

import javax.persistence.*;


@TableGenerator(name = "RawMaterialDiscountT.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "tipodescuentoacopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "tipodescuentoacopiomp")
public class RawMaterialDiscountT implements BaseModel {

    @Id
    @Column(name = "idtipodescuentoacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialDiscountT.tableGenerator")
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "tipo", nullable = true)
    @Enumerated(EnumType.STRING)
    private com.encens.khipus.model.xproduction.RawMaterialDiscountType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public com.encens.khipus.model.xproduction.RawMaterialDiscountType getType() {
        return type;
    }

    public void setType(RawMaterialDiscountType type) {
        this.type = type;
    }
}

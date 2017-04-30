package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("rawMaterialPayRollDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialPayRollDataModel extends QueryDataModel<Long, RawMaterialPayRoll> {

    private PrivateCriteria privateCriteria;

    private Gestion gestion;
    private Month month;
    private Periodo periodo;

    private static final String[] RESTRICTIONS = {
            "upper(productiveZone.name) like concat(concat('%',upper(#{rawMaterialPayRollDataModel.privateCriteria.productiveZone.name})), '%')",
            "upper(productiveZone.group) like concat(concat('%',upper(#{rawMaterialPayRollDataModel.privateCriteria.productiveZone.group})), '%')",
            "productiveZone.number like concat(#{rawMaterialPayRollDataModel.privateCriteria.productiveZone.number}, '%')",
            "rawMaterialPayRoll.startDate >= #{rawMaterialPayRollDataModel.privateCriteria.startDate}",
            "rawMaterialPayRoll.endDate <= #{rawMaterialPayRollDataModel.privateCriteria.endDate}",
            "rawMaterialPayRoll.state = #{rawMaterialPayRollDataModel.privateCriteria.state}"
    };

    @Create
    public void init() {
        sortProperty = "rawMaterialPayRoll.startDate";
    }

    @Override
    public String getEjbql() {
        String query = "select rawMaterialPayRoll " +
                " from RawMaterialPayRoll rawMaterialPayRoll " +
                " join fetch rawMaterialPayRoll.productiveZone productiveZone ";
        return query;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public PrivateCriteria getPrivateCriteria() {
        if (privateCriteria == null) {
            privateCriteria = new PrivateCriteria();
            privateCriteria.setState(StatePayRoll.PENDING);
        }
        return privateCriteria;
    }

    /*@Override
    public void search() {
        Calendar dateIni = Calendar.getInstance();
        Calendar dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(),month.getValue(),periodo.getInitDay());
        dateEnd.set(gestion.getYear(),month.getValue(),periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            privateCriteria.setStartDate(dateFormat.parse(dateFormat.format(dateIni.getTime())));
            privateCriteria.setEndDate(dateFormat.parse(dateFormat.format(dateEnd.getTime())));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (entityQuery != null) {
            entityQuery.refresh();
        }
        setPage(1);
        clearAllSelection();
    }*/

    public void cleanGestionList() {
        setGestion(null);
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        this.periodo = periodo;
    }

    public static class PrivateCriteria {
        private Date startDate;
        private Date endDate;
        private StatePayRoll state;
        private ProductiveZone productiveZone = new ProductiveZone();

        public StatePayRoll getState() {
            return state;
        }

        public void setState(StatePayRoll state) {
            this.state = state;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public ProductiveZone getProductiveZone() {
            return productiveZone;
        }

        public void setProductiveZone(ProductiveZone productiveZone) {
            this.productiveZone = productiveZone;
        }
    }

}
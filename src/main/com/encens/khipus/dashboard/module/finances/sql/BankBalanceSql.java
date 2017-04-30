package com.encens.khipus.dashboard.module.finances.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.21.3
 */
public class BankBalanceSql implements SqlQuery {
    public static enum BankBalanceType {
        BANK("B"),
        COMPANY("E");
        private String type;

        BankBalanceType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    private Integer year = DateUtils.getCurrentYear(new Date());

    private BankBalanceType balanceType = BankBalanceType.COMPANY;

    public String getSql() {
        return "SELECT b.cod_bco ||'_'|| substr(r.mes,5,2) || '_' || c.cta_bco AS code,\n"
                + " b.descri as banco,\n"
                + " c.cta_bco AS cuenta,\n"
                + " DECODE(substr(r.mes,5,2),'01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES,\n"
                + " substr(r.mes,5,2) AS mesnr,\n"
                + " M.abrev AS moneda,\n"
                + " sum(r.mov_mes) saldo\n"
                + " FROM " + Constants.FINANCES_SCHEMA + ".cg_moneda M, " + Constants.FINANCES_SCHEMA + ".ck_bancos b, " + Constants.FINANCES_SCHEMA + ".ck_ctas_bco c, " + Constants.FINANCES_SCHEMA + ".ck_resmes r\n"
                + " WHERE M.no_cia = c.no_cia\n"
                + " AND M.cod_mon = c.moneda\n"
                + " AND b.cod_bco = c.cod_bco\n"
                + " AND c.no_cia = r.no_cia\n"
                + " AND c.cta_bco = r.cta_bco\n"
                + " AND r.no_cia = '01'\n"
                + " AND r.procedencia = '" + balanceType.getType() + "'\n"
                + " AND substr(r.mes,0,4)='" + year + "'\n"
                + " GROUP BY b.cod_bco ||'_'|| substr(r.mes,5,2) || '_' || c.cta_bco, b.descri, c.cta_bco, mes, substr(r.mes,5,2), m.abrev\n"
                + " ORDER BY mesnr asc";
    }

    public void setBalanceType(BankBalanceType balanceType) {
        this.balanceType = balanceType;
    }

    public Integer getYear() {
        return year;
    }
}

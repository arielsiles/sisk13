package com.encens.khipus.dashboard.module.finances.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
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
        return "SELECT b.cod_bco AS code,"
                + " b.descri as banco,"
                + " b.cuenta AS cuenta,"
                + " 'ENERO' AS mes,"
                + " '123' AS mesnr,"
                + " b.moneda AS moneda,"
                + " 10000 AS saldo"
                + " FROM ck_bancos b"
                + " WHERE b.no_cia = '01'";
    }

    public void setBalanceType(BankBalanceType balanceType) {
        this.balanceType = balanceType;
    }

    public Integer getYear() {
        return year;
    }
}

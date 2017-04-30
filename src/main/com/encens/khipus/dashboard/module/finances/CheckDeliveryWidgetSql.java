package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 3.2
 */
public class CheckDeliveryWidgetSql implements SqlQuery {
    private Integer businessUnitId;

    private Integer upperBound = 0;

    private Integer lowerBound = 0;

    @Override
    public String getSql() {
        String sql = "SELECT COUNT(M.NO_TRANS) \n" +
                "FROM " + Constants.FINANCES_SCHEMA + ".CK_DOCUS D \n" +
                "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CK_TIPODOCS T ON T.TIPO_DOC=D.TIPO_DOC AND T.NO_CIA=D.NO_CIA \n" +
                "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CK_MOVS M ON (M.NO_TRANS=D.NO_TRANS AND M.ESTADO=D.ESTADO AND M.NO_CIA= D.NO_CIA) \n" +
                "WHERE \n";
        if (null != businessUnitId) {
            sql += " EXISTS (SELECT DET.NO_TRANS FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVDET DET WHERE DET.NO_CIA=D.NO_CIA AND DET.NO_TRANS=M.NO_TRANS AND DET.TIPO_COMPRO=M.TIPO_COMPRO AND DET.NO_COMPRO=M.NO_COMPRO AND DET.COD_UNI =" + businessUnitId + " ) AND \n";
        }
        sql += "T.TIPO_DOC='CHQ' \n" +
                "AND D.ESTADO='APR' \n" +
                "AND D.ENTREGADO='NO' \n" +
                "AND D.PROCEDENCIA='E' \n" +
                "AND trunc(SYSDATE -M.FECHA) >=" + lowerBound + "\n" +
                "AND trunc(SYSDATE -M.FECHA) <=" + upperBound + "\n";
        return sql;
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    public void setUpperBound(Integer upperBound) {
        this.upperBound = upperBound;
    }

    public void setLowerBound(Integer lowerBound) {
        this.lowerBound = lowerBound;
    }
}

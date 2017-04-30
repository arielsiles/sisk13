package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;

import javax.ejb.Local;

/**
 * @author
 * @version 2.0
 */
@Local
public interface FinancesPkGeneratorService extends GenericService {
    public enum NativeFunction {
        TRANSACTION_NUMBER("getNextSeq('VALE')"),
        TRANSACTION_NUMBER_NOTRANS_TMPENC("getNextSeq('ASIENTO')"),
        TRANSACTION_NUMBER_TMPENC("next_tmpenc()"),
        CONCILIATION_NUMBER("sigte_conci()"),
        NUMBER_ID_SF_TMPENC("newId_sf_tmpenc()");
        private String function;

        private NativeFunction(String function) {
            this.function = function;
        }

        public String getFunction() {
            return function;
        }

    }

    String getNextPK();

    public void setNextPK(String nextNoTrans);

    String getNextTmpenc();

    String getNextNoTransTmpenc();

    String getNextNoTransByDocumentType(String DocumentType);

    String executeFunction(NativeFunction nativeFunction);

    /*Integer newId_sf_tmpenc();

    Integer newId_sf_tmpdet();*/

    Long newId_sf_tmpenc();

    Long newId_sf_tmpdet();

    public Long newId_inv_inventario_detalle();
}

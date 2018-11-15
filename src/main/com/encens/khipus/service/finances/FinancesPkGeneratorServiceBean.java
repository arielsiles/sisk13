package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.util.finances.FinancesUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.Transient;

/**
 * @author
 * @version 2.0
 */
@Stateless
@Name("financesPkGeneratorService")
@AutoCreate
public class FinancesPkGeneratorServiceBean extends GenericServiceBean implements FinancesPkGeneratorService {

    public String getNextPK() {
        return executeFunction(NativeFunction.TRANSACTION_NUMBER);
    }

    @Override
    public void setNextPK(String nextNoTrans) {
        getEntityManager().createNativeQuery("call sp_setSeqVal('VALE',:nextNoTrans)")
                .setParameter("nextNoTrans",nextNoTrans)
                .executeUpdate();
    }

    public String getNextTmpenc() {
        return executeFunction(NativeFunction.TRANSACTION_NUMBER_TMPENC);
    }

    public String getNextNoTransTmpenc() {
        return executeFunction(NativeFunction.TRANSACTION_NUMBER_NOTRANS_TMPENC);
    }

    public String executeFunction(NativeFunction nativeFunction) {
        return (String) getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema(nativeFunction.getFunction()) + " from dual").getSingleResult();
    }

    public String getNextNoTransByDocumentType(String documentType){
        return (String) getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema("getNextSeq('"+documentType+"')") + " from dual").getSingleResult();
    }

    /*public Integer newId_sf_tmpenc(){
        return (Integer)getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema("newId_sf_tmpenc()")).getSingleResult();
    }

    public Integer newId_sf_tmpdet(){
        return (Integer)getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema("newId_sf_tmpdet()")).getSingleResult();
    }*/

    public Long newId_sf_tmpenc(){
        return ((Integer)getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema("newId_sf_tmpenc()")).getSingleResult()).longValue();
    }

    /*public Long newId_sf_tmpenc(){
        Long valor = null;
        valor = (Long)getEntityManager().createNativeQuery("SELECT valor where talbla = 'sf_tmpenc'").getSingleResult();
        getEntityManager().createNativeQuery("update secuencia set valor = valor+1 where tabla = 'sf_tmpenc'").executeUpdate();
        return valor+1;
    }*/

    /** Error **/
    public Long getNextIdSftmpenc(){
        Long valor = null;
        valor = (Long)getEntityManager().createNativeQuery("SELECT valor where talbla = 'sf_tmpenc'").getSingleResult();
        return valor+1;
    }


    public Long newId_sf_tmpdet(){
        return ((Integer)getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema("newId_sf_tmpdet()")).getSingleResult()).longValue();
    }

    public Long newId_inv_inventario_detalle(){
        return ((Integer)getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema("newId_inv_inventario_detalle()")).getSingleResult()).longValue();
    }

}

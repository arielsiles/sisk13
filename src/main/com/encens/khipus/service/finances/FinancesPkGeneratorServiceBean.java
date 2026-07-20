package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.finances.FinancesUtil;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.OptimisticLockException;

/**
 * @author
 * @version 2.0
 */
@Stateless
@Name("financesPkGeneratorService")
@AutoCreate
public class FinancesPkGeneratorServiceBean extends GenericServiceBean implements FinancesPkGeneratorService {

    /** Nombre base de la secuencia del numero de transaccion (no_trans) de asientos. **/
    private static final String ACCOUNTING_TRANSACTION_SEQUENCE = "ASIENTO";
    /** Nombre base de la secuencia de numero de transaccion de vales (antes getNextSeq('VALE')). **/
    private static final String WAREHOUSE_VOUCHER_SEQUENCE = "VALE";
    private static final int MAX_SEQUENCE_TRIES = 10;

    @In(create = true)
    private FinancesSequenceService financesSequenceService;

    /**
     * Numero de transaccion de vales (VALE). Se genera con la entidad JPA
     * FinancesSequence por compania real, reemplazando getNextSeq('VALE'). La funcion
     * almacenada queda sin uso.
     */
    public String getNextPK() {
        return String.valueOf(nextFinancesSequence(WAREHOUSE_VOUCHER_SEQUENCE));
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

    /**
     * Numero de transaccion (no_trans) de asientos contables. Se genera con la entidad
     * JPA FinancesSequence (tabla '_sequence') POR COMPANIA real, con control optimista
     * (version) y reintento, reemplazando la funcion almacenada getNextSeq('ASIENTO')
     * que hacia SELECT+UPDATE sin bloqueo y podia duplicar el numero. La funcion queda
     * como respaldo.
     */
    public String getNextNoTransTmpenc() {
        return String.valueOf(nextFinancesSequence(ACCOUNTING_TRANSACTION_SEQUENCE));
    }

    /**
     * Numero de documento (no_doc) de un asiento contable, por tipo de documento y por
     * compania. Mismo mecanismo JPA seguro que no_trans.
     */
    public String getNextDocumentNumberByType(String documentType) {
        return String.valueOf(nextFinancesSequence(documentType));
    }

    /**
     * Genera el siguiente correlativo de finanzas por compania, reintentando ante
     * colisiones optimistas (OptimisticLockException). El incremento corre en
     * transaccion propia (FinancesSequenceService, REQUIRES_NEW) para que cada
     * reintento use una transaccion limpia. Sirve para todos los correlativos que
     * antes daba getNextSeq() (asientos, vales, ventas...).
     */
    private long nextFinancesSequence(String sequenceName) {
        Long companyId = resolveCurrentCompanyId();
        int tries = 0;
        while (true) {
            try {
                return financesSequenceService.nextValue(sequenceName, companyId);
            } catch (EJBException e) {
                if (e.getCausedByException() instanceof OptimisticLockException && ++tries < MAX_SEQUENCE_TRIES) {
                    continue;
                }
                throw e;
            }
        }
    }

    /**
     * Compania real de la sesion (la misma que CompanyListener estampa en las entidades).
     * En procesos batch sin sesion se usa la compania por defecto.
     */
    private Long resolveCurrentCompanyId() {
        Company currentCompany = (Company) Component.getInstance("currentCompany");
        return (currentCompany != null && currentCompany.getId() != null)
                ? currentCompany.getId()
                : Constants.defaultCompanyId;
    }

    public String executeFunction(NativeFunction nativeFunction) {
        return (String) getEntityManager().createNativeQuery("select " + FinancesUtil.addSchema(nativeFunction.getFunction()) + " from dual").getSingleResult();
    }

    /**
     * Correlativo por tipo (ventas: VENTADIRECTA/SECUENCIAPEDIDO, etc.). Migrado de
     * getNextSeq(<tipo>) a la entidad JPA FinancesSequence por compania real.
     */
    public String getNextNoTransByDocumentType(String documentType){
        return String.valueOf(nextFinancesSequence(documentType));
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

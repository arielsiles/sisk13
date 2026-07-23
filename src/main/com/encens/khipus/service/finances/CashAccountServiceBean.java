package com.encens.khipus.service.finances;

import com.encens.khipus.action.finances.dto.AccountLevelErrorDTO;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountPk;
import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * CashAccountServiceBean
 *
 * @author
 * @version 2.0
 */
@Name("cashAccountService")
@Stateless
@AutoCreate
public class CashAccountServiceBean implements CashAccountService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    /**
     * Inventario de tablas/columnas que referencian un codigo de cuenta (arcgms.cuenta).
     * Derivado del mapeo JPA (@JoinColumn referencedColumnName="cuenta") + configuracion
     * + la jerarquia del propio arcgms. Cada fila: {tabla, col1, col2, ...}.
     * Usado por findAccountReferences para saber si una cuenta esta en uso.
     * NOTA: solo se consultan las columnas que existan en el esquema actual
     * (se filtran contra information_schema), asi que sobra/falta no rompe.
     */
    private static final String[][] ACCOUNT_REFERENCES = {
            {"af_movs", "cuenta"},
            {"af_pago", "cuentacaja"},
            {"af_subgrupos", "cta_alm", "cta_vo", "ctadavo", "ctagavo", "ctamej"},
            {"arcgms", "cta_raiz", "cta_niv3"},
            {"categoriapuesto", "codctactbdebe", "codctactbdebeme", "codctactbhaber", "codctactbhaberme",
                    "codctagastoaguimn", "codctagastoindemmn", "codctaprevindemme", "codctaprevindemmn",
                    "codctaprovaguime", "codctaprovaguimn", "fonpenctapatronal", "segsocctapatronal"},
            {"cg_detplanti", "cuenta"},
            {"cg_movdet", "cuenta"},
            {"ck_ctas_bco", "cuenta"},
            {"cobrofondorota", "cuentaajuste", "cuentacaja"},
            {"com_encoc", "cuentapago"},
            {"configuracion", "cajagral1mn", "ct_cajaahorro", "ct_cajaveter", "cta_pat01", "cta_pat02",
                    "cta_pat03", "cta_pat04", "cta_pat05", "ctaafet", "ctaaitb", "ctaalmme", "ctaalmmn",
                    "ctaalmpt", "ctaalmptag", "ctaalmpv", "ctaantprovme", "ctaantprovmn", "ctacomision",
                    "ctacostpt", "ctacostpv", "ctadeptrame", "ctadeptramn", "ctadiftipcam", "ctag_it",
                    "ctai_ventapri", "ctai_ventasec", "ctaivacrefime", "ctaivacrefimn", "ctaivacrefitrmn",
                    "ctamerma", "ctamermabaj", "ctap_debfisiva", "ctap_itxpagar", "ctaprom", "ctaprovaf",
                    "ctaprovobu", "ctareproc", "ctatransalm1mn", "ctatransalm2mn", "ctatransalmme",
                    "ctatransalmmn", "cxp_cns", "cxp_iva", "cxp_provmn", "cxp_regalia", "i_pvig_pf_mn",
                    "it_ret", "iue_ret", "oc_pagodefault", "res_perdida", "res_utilidad"},
            {"costosindirectosconf", "cuenta"},
            {"cxp_docus", "ctaxpagar"},
            {"cxp_proveedores", "ctaxpagar"},
            {"distribuciongasto", "cuenta"},
            {"distribuciongastocobrofon", "cuenta"},
            {"documentocompra", "cuentaajuste"},
            {"fondorotatorio", "cuentactb"},
            {"inv_almacenes", "cuenta"},
            {"inv_articulos", "cuenta_art"},
            {"inv_grupos", "cta_baja", "cta_costo", "cta_gasto", "cuenta_inv"},
            {"inv_movdet", "cuenta_art"},
            {"inv_tipodocs", "ctacosto", "ctracuentamn"},
            {"inv_vales", "contracuenta", "cta_gasto"},
            {"pagofondorota", "cuentaajuste", "cuentacaja"},
            {"pagoordencompra", "cuentacaja", "cuentarendir"},
            {"registrocontable", "cuentaxpagar"},
            {"sf_tmpdet", "cuenta"},
            {"sf_tmpenc", "cuenta"},
            {"tipocaja", "cuentacaja", "cuentacontingencia", "cuentaingreso", "cuentaxcobrar"},
            {"tipocredito", "ctaeje", "ctaven", "ctavig", "ictaeje", "ictaven", "ictavig", "ipctaeje", "ipctaven"},
            {"tipodocfondorota", "cuentactbajme", "cuentactbajmn", "cuentactbme", "cuentactbmn"},
            {"tipomovsueldo", "cuentactb"},
            {"tipoplantillacontablepredefinida", "cuenta"}
    };

    public Boolean existsAccount(String accountCode) {
        try {
            return em.createNamedQuery("CashAccount.findByAccountCode")
                    .setParameter("accountCode", accountCode).getSingleResult() != null;
        } catch (EntityNotFoundException e) {
        }
        return false;
    }

    public CashAccount findByAccountCode(String accountCode) {
        try {
            return (CashAccount) em.createNamedQuery("CashAccount.findByAccountCode")
                    .setParameter("accountCode", accountCode).getSingleResult();
        } catch (EntityNotFoundException e) {
        } catch (NoResultException e) {
            // getSingleResult() lanza NoResultException, no EntityNotFoundException:
            // sin este catch el metodo propagaba en vez de devolver null.
        }
        return null;
    }

    public Boolean isActiveAccount(String accountCode) {
        try {
            return em.createNamedQuery("CashAccount.findByActiveAccount")
                    .setParameter("accountCode", accountCode)
                    .setParameter("active", Boolean.TRUE).getSingleResult() != null;
        } catch (EntityNotFoundException e) {
        }
        return false;
    }

    public List<CashAccount> findCashAccountList(){
        try {
            return em.createNamedQuery("CashAccount.findCashAccountList").getResultList();
        } catch (NoResultException e) {
            return new ArrayList<CashAccount>();
        }
    }

    public List<CashAccount> findCashAccountListByType(CashAccountType accountType){
        try {
            return em.createNamedQuery("CashAccount.findCashAccountListByType")
                    .setParameter("accountType", accountType)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<CashAccount>();
        }
    }

    @Override
    public Boolean createCashAccount(CashAccount cashAccount) {

        // Duplicado: en la BD la PK real de arcgms es SOLO `cuenta` (la entidad
        // modela @EmbeddedId{no_cia, cuenta}, que sobre-modela la clave). Por eso
        // se valida por codigo, sin compania: es la unicidad que aplica el motor.
        if (accountCodeExists(cashAccount.getAccountCode())) {
            return Boolean.FALSE; // cuenta duplicada
        }

        // Fijar la PK: el codigo viene del formulario (campo espejo accountCode,
        // insertable=false), hay que copiarlo al @EmbeddedId. La compania la pone
        // CompanyNumberListener en @PrePersist; se setea explicita por claridad.
        cashAccount.setId(new CashAccountPk(Constants.defaultCompanyNumber, cashAccount.getAccountCode()));

        // Columnas escalares que se escriben (las relaciones root/nivel3 estan
        // mapeadas insertable=false; el valor real va en estos campos String).
        cashAccount.setRootAccountCode(cashAccount.getRootCashAccount() != null
                ? cashAccount.getRootCashAccount().getAccountCode() : cashAccount.getRootAccountCode());
        cashAccount.setAccountLevel3Code(cashAccount.getCashAccountLeve3() != null
                ? cashAccount.getCashAccountLeve3().getAccountCode() : cashAccount.getAccountLevel3Code());

        // Evitar NULL en los flags donde el insert anterior ponia 'S'/'N'.
        if (cashAccount.getActive() == null) cashAccount.setActive(Boolean.TRUE);
        if (cashAccount.getMovementAccount() == null) cashAccount.setMovementAccount(Boolean.FALSE);
        if (cashAccount.getRegulating() == null) cashAccount.setRegulating(Boolean.FALSE);
        if (cashAccount.getUtil() == null) cashAccount.setUtil(Boolean.FALSE);
        if (cashAccount.getHasWarehousePermission() == null) cashAccount.setHasWarehousePermission(Boolean.FALSE);
        if (cashAccount.getHasCostCenter() == null) cashAccount.setHasCostCenter(Boolean.FALSE);
        if (cashAccount.getAllowIva() == null) cashAccount.setAllowIva(Boolean.FALSE);

        try {
            em.persist(cashAccount);
            em.flush();
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }

    @Override
    public List<CashAccount> findCashAccountsUtil() {
        List<CashAccount> result;
        try {
            result = em.createNamedQuery("CashAccount.findAccountsUtil")
                    .setParameter("util", Boolean.TRUE)
                    .getResultList();
        } catch (NoResultException e) {
            result = new ArrayList<CashAccount>();
        }

        for (CashAccount cashAccount : result) {
            System.out.println("===> Account util: " + cashAccount.getFullName());
        }


        return result;
    }

    // ------------------------------------------------------------------------
    // Analisis y correccion de niveles del plan de cuentas (cta_raiz / cta_niv3)
    //
    // Reglas (identicas a los scripts scripts/analyze_arcgms*.py y a los
    // documentos ARCGMS_CTA_RAIZ_CORRECCION.md / ARCGMS_CTA_NIV3_CORRECCION.md):
    //   - Solo se procesan cuentas de 10 digitos. Otra longitud => se informa,
    //     no se corrige (findAccountFormatErrors).
    //   - cta_raiz  = 2 primeros digitos + '00000000'. Se omite si esta vacia.
    //   - cta_niv3  = '' en niveles 1-2; 3 primeros digitos + '0000000' en 3+.
    //
    // Se usan native queries (dialecto MySQL) sobre la tabla arcgms sin prefijo
    // de esquema, igual que createCashAccount() de arriba. Opera sobre TODAS las
    // companias (no_cia) porque la formula depende solo del codigo de cuenta.
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public List<AccountLevelErrorDTO> findAccountFormatErrors() {
        List<Object[]> rows = em.createNativeQuery(
                "select no_cia, cuenta, descri, cn_nivel, char_length(cuenta) " +
                "from arcgms " +
                "where char_length(cuenta) <> 10 " +
                "order by no_cia, cuenta").getResultList();

        List<AccountLevelErrorDTO> errors = new ArrayList<AccountLevelErrorDTO>();
        for (Object[] row : rows) {
            errors.add(new AccountLevelErrorDTO(
                    toStr(row[0]), toStr(row[1]), toStr(row[2]), toInt(row[3]),
                    toStr(row[4]), "10"));
        }
        return errors;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AccountLevelErrorDTO> findRootAccountErrors() {
        // La 7ma columna (targetExists) indica si la cuenta raiz calculada
        // existe en la misma compania. Si no existe, la fila no se corregira.
        List<Object[]> rows = em.createNativeQuery(
                "select a.no_cia, a.cuenta, a.descri, a.cn_nivel, a.cta_raiz, " +
                "       concat(substring(a.cuenta,1,2),'00000000'), " +
                "       exists(select 1 from arcgms p " +
                "              where p.no_cia = a.no_cia " +
                "                and p.cuenta = concat(substring(a.cuenta,1,2),'00000000')) " +
                "from arcgms a " +
                "where char_length(a.cuenta) = 10 " +
                "  and a.cta_raiz is not null and a.cta_raiz <> '' " +
                "  and a.cta_raiz <> concat(substring(a.cuenta,1,2),'00000000') " +
                "order by a.no_cia, a.cuenta").getResultList();

        List<AccountLevelErrorDTO> errors = new ArrayList<AccountLevelErrorDTO>();
        for (Object[] row : rows) {
            errors.add(new AccountLevelErrorDTO(
                    toStr(row[0]), toStr(row[1]), toStr(row[2]), toInt(row[3]),
                    toStr(row[4]), toStr(row[5]), toBool(row[6])));
        }
        return errors;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AccountLevelErrorDTO> findLevel3AccountErrors() {
        // Para niveles 1-2 el destino correcto es vacio (limpiar): targetExists = 1.
        // Para niveles 3+ se valida que la cuenta nivel 3 calculada exista.
        List<Object[]> rows = em.createNativeQuery(
                "select a.no_cia, a.cuenta, a.descri, a.cn_nivel, coalesce(a.cta_niv3,''), " +
                "       case when a.cn_nivel in (1,2) then '' " +
                "            else concat(substring(a.cuenta,1,3),'0000000') end, " +
                "       case when a.cn_nivel in (1,2) then 1 " +
                "            else exists(select 1 from arcgms p " +
                "                        where p.no_cia = a.no_cia " +
                "                          and p.cuenta = concat(substring(a.cuenta,1,3),'0000000')) end " +
                "from arcgms a " +
                "where char_length(a.cuenta) = 10 " +
                "  and ( (a.cn_nivel in (1,2) and a.cta_niv3 is not null and a.cta_niv3 <> '') " +
                "     or (a.cn_nivel not in (1,2) " +
                "         and coalesce(a.cta_niv3,'') <> concat(substring(a.cuenta,1,3),'0000000')) ) " +
                "order by a.no_cia, a.cuenta").getResultList();

        List<AccountLevelErrorDTO> errors = new ArrayList<AccountLevelErrorDTO>();
        for (Object[] row : rows) {
            errors.add(new AccountLevelErrorDTO(
                    toStr(row[0]), toStr(row[1]), toStr(row[2]), toInt(row[3]),
                    toStr(row[4]), toStr(row[5]), toBool(row[6])));
        }
        return errors;
    }

    @Override
    public int[] fixAccountLevelErrors() {
        // cta_raiz: el JOIN contra arcgms garantiza que solo se actualiza si la
        // cuenta raiz calculada EXISTE (misma compania). Si no existe, la fila
        // no entra en el update y queda marcada como error en el analisis.
        int rootFixed = em.createNativeQuery(
                "update arcgms a " +
                "  join arcgms p on p.no_cia = a.no_cia " +
                "               and p.cuenta = concat(substring(a.cuenta,1,2),'00000000') " +
                "set a.cta_raiz = p.cuenta " +
                "where char_length(a.cuenta) = 10 " +
                "  and a.cta_raiz is not null and a.cta_raiz <> '' " +
                "  and a.cta_raiz <> concat(substring(a.cuenta,1,2),'00000000')").executeUpdate();

        // cta_niv3 niveles 1-2: limpiar (vacio). No requiere validar existencia.
        int level3Cleared = em.createNativeQuery(
                "update arcgms " +
                "set cta_niv3 = '' " +
                "where char_length(cuenta) = 10 " +
                "  and cn_nivel in (1,2) " +
                "  and cta_niv3 is not null and cta_niv3 <> ''").executeUpdate();

        // cta_niv3 niveles 3+: JOIN garantiza que la cuenta nivel 3 calculada exista.
        int level3Fixed = em.createNativeQuery(
                "update arcgms a " +
                "  join arcgms p on p.no_cia = a.no_cia " +
                "               and p.cuenta = concat(substring(a.cuenta,1,3),'0000000') " +
                "set a.cta_niv3 = p.cuenta " +
                "where char_length(a.cuenta) = 10 " +
                "  and a.cn_nivel not in (1,2) " +
                "  and coalesce(a.cta_niv3,'') <> concat(substring(a.cuenta,1,3),'0000000')").executeUpdate();

        return new int[]{rootFixed, level3Cleared + level3Fixed};
    }

    @Override
    public boolean accountCodeExists(String accountCode) {
        Number count = (Number) em.createNativeQuery(
                "select count(*) from arcgms where cuenta = :code")
                .setParameter("code", accountCode)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> findMissingAccountCodes(Collection<String> accountCodes) {
        List<String> missing = new ArrayList<String>();
        if (accountCodes == null || accountCodes.isEmpty()) {
            return missing;
        }
        Set<String> distinct = new LinkedHashSet<String>(accountCodes);
        List<String> found = em.createQuery(
                "select ca.accountCode from CashAccount ca where ca.accountCode in (:codes)")
                .setParameter("codes", distinct)
                .getResultList();
        Set<String> existing = new HashSet<String>(found);
        for (String code : distinct) {
            if (!existing.contains(code)) {
                missing.add(code);
            }
        }
        return missing;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> findAccountReferences(String accountCode) {
        // 1) Columnas que realmente existen en el esquema actual, para no consultar
        //    columnas ausentes (que romperian la consulta).
        Set<String> existing = new HashSet<String>();
        List<Object[]> schemaCols = em.createNativeQuery(
                "select lower(table_name), lower(column_name) " +
                "from information_schema.columns where table_schema = database()").getResultList();
        for (Object[] row : schemaCols) {
            existing.add(toStr(row[0]) + "." + toStr(row[1]));
        }

        // 2) Por cada tabla del inventario, consultar si el codigo aparece en alguna
        //    de sus columnas de cuenta (que existan). Devuelve las tablas donde se usa.
        List<String> references = new ArrayList<String>();
        for (String[] entry : ACCOUNT_REFERENCES) {
            String table = entry[0];
            StringBuilder condition = new StringBuilder();
            for (int i = 1; i < entry.length; i++) {
                String column = entry[i];
                if (!existing.contains(table + "." + column)) {
                    continue;
                }
                if (condition.length() > 0) {
                    condition.append(" or ");
                }
                condition.append(column).append(" = :code");
            }
            if (condition.length() == 0) {
                continue;
            }
            String where = condition.toString();
            // En arcgms se excluye la propia cuenta: solo cuenta si OTRA cuenta la
            // referencia como raiz/nivel 3.
            if ("arcgms".equals(table)) {
                where = "(" + where + ") and cuenta <> :code";
            }
            boolean used = !em.createNativeQuery("select 1 from " + table + " where " + where + " limit 1")
                    .setParameter("code", accountCode)
                    .getResultList().isEmpty();
            if (used) {
                references.add(table);
            }
        }
        return references;
    }

    @Override
    public void renameCashAccount(String companyNumber, String oldAccountCode, String newAccountCode) {
        em.createNativeQuery("update arcgms set cuenta = :newCode where no_cia = :company and cuenta = :oldCode")
                .setParameter("newCode", newAccountCode)
                .setParameter("company", companyNumber)
                .setParameter("oldCode", oldAccountCode)
                .executeUpdate();
    }

    private String toStr(Object value) {
        return value == null ? "" : value.toString();
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Mapea el resultado de un EXISTS(...) nativo (1/0, true/false) a boolean.
     */
    private boolean toBool(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        String s = value.toString().trim();
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s);
    }

}

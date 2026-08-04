package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.DanglingAccount;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author
 * @version 1.0
 */
@Name("accountIntegrityService")
@Stateless
@AutoCreate
public class AccountIntegrityServiceBean extends GenericServiceBean implements AccountIntegrityService {

    /**
     * Columnas de cuenta de `configuracion`, descubiertas por reflexion sobre las
     * asociaciones CashAccount de CompanyConfiguration (@JoinColumn con
     * referencedColumnName="cuenta"). Se cachea y ademas sirve de whitelist: solo
     * estas columnas pueden ir en un UPDATE, evitando inyeccion via nombre.
     */
    private Set<String> configAccountColumns() {
        Set<String> columns = new LinkedHashSet<String>();
        for (Field field : CompanyConfiguration.class.getDeclaredFields()) {
            if (!CashAccount.class.equals(field.getType())) {
                continue;
            }
            JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
            if (joinColumns == null) {
                continue;
            }
            for (JoinColumn joinColumn : joinColumns.value()) {
                if ("cuenta".equals(joinColumn.referencedColumnName())) {
                    columns.add(joinColumn.name());
                }
            }
        }
        return columns;
    }

    @SuppressWarnings("unchecked")
    public List<DanglingAccount> findDanglingAccounts() {
        List<DanglingAccount> result = new ArrayList<DanglingAccount>();
        result.addAll(findDanglingConfig());
        result.addAll(findDanglingBank());
        return result;
    }

    public boolean hasDangling() {
        return !findDanglingAccounts().isEmpty();
    }

    /**
     * Columnas de `configuracion` con un codigo que no resuelve en arcgms por
     * (no_cia, cuenta). configuracion tiene una fila por compania.
     */
    @SuppressWarnings("unchecked")
    private List<DanglingAccount> findDanglingConfig() {
        List<DanglingAccount> list = new ArrayList<DanglingAccount>();
        Set<String> columns = configAccountColumns();
        if (columns.isEmpty()) {
            return list;
        }

        StringBuilder union = new StringBuilder();
        for (String col : columns) {
            if (union.length() > 0) {
                union.append(" UNION ALL ");
            }
            // col viene de la whitelist (reflexion sobre @JoinColumn), no del usuario.
            union.append("SELECT '").append(col).append("' AS columna, no_cia, `")
                    .append(col).append("` AS codigo FROM configuracion");
        }

        String sql = "SELECT t.columna, t.no_cia, t.codigo FROM (" + union + ") t"
                + " WHERE t.codigo IS NOT NULL AND TRIM(t.codigo) <> ''"
                + " AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = t.no_cia AND a.cuenta = t.codigo)";

        List<Object[]> rows = getEntityManager().createNativeQuery(sql).getResultList();
        for (Object[] row : rows) {
            DanglingAccount d = new DanglingAccount();
            d.setOrigin(DanglingAccount.ORIGIN_CONFIG);
            d.setColumn(asString(row[0]));
            d.setCompanyNumber(asString(row[1]));
            d.setCode(asString(row[2]));
            list.add(d);
        }
        return list;
    }

    /**
     * Cuentas bancarias (ck_ctas_bco) cuya cuenta contable no resuelve en arcgms.
     * Se listan todas (VIG y no VIG): las que rompen Preferencias hoy son las VIG,
     * pero como diagnostico conviene ver todas.
     */
    @SuppressWarnings("unchecked")
    private List<DanglingAccount> findDanglingBank() {
        List<DanglingAccount> list = new ArrayList<DanglingAccount>();
        String sql = "SELECT b.no_cia, b.cta_bco, b.descri, b.estado, b.cuenta"
                + " FROM ck_ctas_bco b"
                + " WHERE b.cuenta IS NOT NULL AND TRIM(b.cuenta) <> ''"
                + " AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = b.no_cia AND a.cuenta = b.cuenta)";

        List<Object[]> rows = getEntityManager().createNativeQuery(sql).getResultList();
        for (Object[] row : rows) {
            DanglingAccount d = new DanglingAccount();
            d.setOrigin(DanglingAccount.ORIGIN_BANK);
            d.setCompanyNumber(asString(row[0]));
            d.setBankAccountNumber(asString(row[1]));
            d.setDescription(asString(row[2]));
            d.setState(asString(row[3]));
            d.setColumn("cuenta");
            d.setCode(asString(row[4]));
            list.add(d);
        }
        return list;
    }

    /**
     * Anula (NULL) el codigo colgado. Corre en la transaccion del request, que
     * commitea al final (la pantalla ya no usa flush-mode MANUAL). NO se usa
     * REQUIRES_NEW: al llamarse en un loop, un extended persistence context de Seam
     * no tolera abrir una sub-transaccion nueva por cada item -- la segunda fallaba
     * y por eso solo se anulaba una cuenta.
     */
    public boolean nullify(DanglingAccount item) {
        if (item == null) {
            return false;
        }
        try {
            Query query;
            if (item.isBank()) {
                query = getEntityManager().createNativeQuery(
                        "UPDATE ck_ctas_bco SET cuenta = NULL WHERE no_cia = :noCia AND cta_bco = :ctaBco");
                query.setParameter("noCia", item.getCompanyNumber());
                query.setParameter("ctaBco", item.getBankAccountNumber());
            } else {
                // Whitelist: la columna debe ser una de las asociaciones conocidas.
                if (!configAccountColumns().contains(item.getColumn())) {
                    log.error("Columna de cuenta desconocida, no se anula: " + item.getColumn());
                    return false;
                }
                query = getEntityManager().createNativeQuery(
                        "UPDATE configuracion SET `" + item.getColumn() + "` = NULL"
                                + " WHERE no_cia = :noCia AND `" + item.getColumn() + "` = :code");
                query.setParameter("noCia", item.getCompanyNumber());
                query.setParameter("code", item.getCode());
            }
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            // p.ej. columna NOT NULL en la BD del cliente: se reporta, no se rompe.
            log.error("No se pudo anular la cuenta colgada (" + item.getOrigin() + "/"
                    + item.getColumn() + ")", e);
            return false;
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}

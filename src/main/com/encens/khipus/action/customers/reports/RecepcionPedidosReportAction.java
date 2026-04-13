package com.encens.khipus.action.customers.reports;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.customers.Territoriotrabajo;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseType;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Name("recepcionPedidosReportAction")
@Scope(ScopeType.PAGE)
public class RecepcionPedidosReportAction {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private User currentUser;

    @In
    private CompanyConfigurationService companyConfigurationService;

    private Date fechaEntrega;
    private List<Territoriotrabajo> selectedTerritorios;
    private Warehouse warehouse;

    @Create
    public void init() {
        loadDefaultWarehouse();
    }

    @SuppressWarnings("unchecked")
    private void loadDefaultWarehouse() {
        try {
            List<Warehouse> list = em.createQuery(
                    "SELECT w FROM Warehouse w WHERE w.warehouseType = :tipo")
                    .setParameter("tipo", WarehouseType.DAIRY)
                    .setMaxResults(1)
                    .getResultList();
            if (!list.isEmpty()) {
                warehouse = list.get(0);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public void resetFilters() {
        fechaEntrega = null;
        selectedTerritorios = null;
        loadDefaultWarehouse();
    }

    public void onFechaChanged() {
        selectedTerritorios = null;
    }

    @SuppressWarnings("unchecked")
    public void generateReport() throws IOException, JRException {

        if (fechaEntrega == null) return;
        if (warehouse == null) return;

        String warehouseCode = warehouse.getId().getWarehouseCode();

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT CONCAT(co.client.name,' ',co.client.lastName,' ',COALESCE(co.client.maidenName,'')),");
        jpql.append(" co.code,");
        jpql.append(" ao.productItem.nameShort,");
        jpql.append(" (COALESCE(ao.quantity,0) + COALESCE(ao.reposicion,0) + COALESCE(ao.promotion,0)),");
        jpql.append(" co.client.territoriotrabajo.nombre");
        jpql.append(" FROM CustomerOrder co JOIN co.articleOrderList ao");
        jpql.append(" WHERE co.orderDate = :fechaEntrega");
        jpql.append(" AND co.state <> :estadoAnulado");
        jpql.append(" AND co.saleType = :tipoVenta");
        jpql.append(" AND ao.productItem.warehouseCode = :warehouseCode");

        boolean filterByTerritory = selectedTerritorios != null && !selectedTerritorios.isEmpty();
        if (filterByTerritory) {
            jpql.append(" AND co.client.territoriotrabajo IN (:territorios)");
        }

        javax.persistence.Query query = em.createQuery(jpql.toString())
                .setParameter("fechaEntrega", fechaEntrega, TemporalType.DATE)
                .setParameter("estadoAnulado", SaleStatus.ANULADO)
                .setParameter("tipoVenta", SaleTypeEnum.CREDIT)
                .setParameter("warehouseCode", warehouseCode);

        if (filterByTerritory) {
            query.setParameter("territorios", selectedTerritorios);
        }

        List<Object[]> results = query.getResultList();

        Collection<Map<String, ?>> rows = new ArrayList<Map<String, ?>>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("cliente", row[0] != null ? row[0].toString() : "");
            map.put("nota", row[1] != null ? row[1].toString() : "");
            map.put("producto", row[2] != null ? row[2].toString() : "");
            map.put("cantidad", row[3] != null ? ((Number) row[3]).intValue() : 0);
            map.put("distribuidor", row[4] != null ? row[4].toString() : "");
            rows.add(map);
        }

        int cantidadPedidos = countPedidos(warehouseCode, filterByTerritory);
        BigDecimal importe = calculateImporte(warehouseCode, filterByTerritory);

        String title = "";
        String companyName = "";
        try {
            CompanyConfiguration cc = companyConfigurationService.findCompanyConfiguration();
            title = cc.getTitle();
            companyName = cc.getCompanyName();
        } catch (CompanyConfigurationNotFoundException e) {
            // ignore
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fecha", df.format(fechaEntrega));
        params.put("cantidadPedidos", String.valueOf(cantidadPedidos));
        params.put("nomUsr", currentUser.getUsername());
        params.put("nombreTerritorio", buildTerritorioLabel());
        params.put("title", title);
        params.put("companyName", companyName);
        params.put("importe", importe);

        InputStream jrxmlStream = JSFUtil.getResourceAsStream("/customers/reports/recepcionPedidos.jrxml");
        byte[] jrxmlBytes = readAllBytes(jrxmlStream);
        String jrxmlContent = new String(jrxmlBytes, "UTF-8");
        jrxmlContent = jrxmlContent.replace(" uuid=\"", " _uuid=\"");
        jrxmlContent = jrxmlContent.replaceAll(" _uuid=\"[^\"]*\"", "");
        jrxmlContent = jrxmlContent.replaceAll("<bucket class=\"[^\"]*\">", "<bucket>");
        jrxmlContent = jrxmlContent.replace("<bucketExpression>", "<bucketExpression class=\"java.lang.String\">");
        jrxmlContent = jrxmlContent.replace("<measureExpression class=", "<measureExpression _class=");
        jrxmlContent = jrxmlContent.replaceAll("<measureExpression _class=\"[^\"]*\">", "<measureExpression>");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$P{importe}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$P{importe}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{cantidadMeasure}", "<textFieldExpression class=\"java.lang.Integer\"><![CDATA[$V{cantidadMeasure}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression>", "<textFieldExpression class=\"java.lang.String\">");
        InputStream cleanStream = new java.io.ByteArrayInputStream(jrxmlContent.getBytes("UTF-8"));
        JasperReport jasperReport = JasperCompileManager.compileReport(cleanStream);
        JRDataSource dataSource = new JRMapCollectionDataSource(rows);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=RecepcionPedidos.pdf");
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    private int countPedidos(String warehouseCode, boolean filterByTerritory) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT COUNT(DISTINCT co.id) FROM CustomerOrder co JOIN co.articleOrderList ao");
        jpql.append(" WHERE co.orderDate = :fechaEntrega");
        jpql.append(" AND co.state <> :estadoAnulado");
        jpql.append(" AND co.saleType = :tipoVenta");
        jpql.append(" AND ao.productItem.warehouseCode = :warehouseCode");
        if (filterByTerritory) {
            jpql.append(" AND co.client.territoriotrabajo IN (:territorios)");
        }

        javax.persistence.Query query = em.createQuery(jpql.toString())
                .setParameter("fechaEntrega", fechaEntrega, TemporalType.DATE)
                .setParameter("estadoAnulado", SaleStatus.ANULADO)
                .setParameter("tipoVenta", SaleTypeEnum.CREDIT)
                .setParameter("warehouseCode", warehouseCode);
        if (filterByTerritory) {
            query.setParameter("territorios", selectedTerritorios);
        }

        Long count = (Long) query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    private BigDecimal calculateImporte(String warehouseCode, boolean filterByTerritory) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT COALESCE(SUM(ao.amount), 0.0) FROM CustomerOrder co JOIN co.articleOrderList ao");
        jpql.append(" WHERE co.orderDate = :fechaEntrega");
        jpql.append(" AND co.state <> :estadoAnulado");
        jpql.append(" AND co.saleType = :tipoVenta");
        jpql.append(" AND ao.productItem.warehouseCode = :warehouseCode");
        if (filterByTerritory) {
            jpql.append(" AND co.client.territoriotrabajo IN (:territorios)");
        }

        javax.persistence.Query query = em.createQuery(jpql.toString())
                .setParameter("fechaEntrega", fechaEntrega, TemporalType.DATE)
                .setParameter("estadoAnulado", SaleStatus.ANULADO)
                .setParameter("tipoVenta", SaleTypeEnum.CREDIT)
                .setParameter("warehouseCode", warehouseCode);
        if (filterByTerritory) {
            query.setParameter("territorios", selectedTerritorios);
        }

        Object result = query.getSingleResult();
        if (result instanceof BigDecimal) return (BigDecimal) result;
        if (result instanceof Double) return BigDecimal.valueOf((Double) result);
        return BigDecimal.ZERO;
    }

    private String buildTerritorioLabel() {
        if (selectedTerritorios == null || selectedTerritorios.isEmpty()) {
            return "Todos";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedTerritorios.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(selectedTerritorios.get(i).getNombre());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public List<Territoriotrabajo> getTerritoriosConPedidos() {
        if (fechaEntrega == null) return new ArrayList<Territoriotrabajo>();

        StringBuilder sub = new StringBuilder();
        sub.append("SELECT DISTINCT co.client.territoriotrabajo FROM CustomerOrder co JOIN co.articleOrderList ao");
        sub.append(" WHERE co.orderDate = :fechaEntrega");
        sub.append(" AND co.state <> :estadoAnulado");
        sub.append(" AND co.saleType = :tipoVenta");
        if (warehouse != null) {
            sub.append(" AND ao.productItem.warehouseCode = :warehouseCode");
        }

        javax.persistence.Query query = em.createQuery(
                "SELECT t FROM Territoriotrabajo t WHERE t IN (" + sub + ") ORDER BY t.nombre")
                .setParameter("fechaEntrega", fechaEntrega, TemporalType.DATE)
                .setParameter("estadoAnulado", SaleStatus.ANULADO)
                .setParameter("tipoVenta", SaleTypeEnum.CREDIT);
        if (warehouse != null) {
            query.setParameter("warehouseCode", warehouse.getId().getWarehouseCode());
        }
        return query.getResultList();
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public List<Territoriotrabajo> getSelectedTerritorios() {
        return selectedTerritorios;
    }

    public void setSelectedTerritorios(List<Territoriotrabajo> selectedTerritorios) {
        this.selectedTerritorios = selectedTerritorios;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toByteArray();
    }
}

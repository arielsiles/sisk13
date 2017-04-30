package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the general valued evolution report action
 *
 * @author
 * @version 2.26
 */

@Name("generalValuedEvolutionReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('VALUEDWAREHOUSERESIDUEREPORT','VIEW')}")
public class GeneralValuedEvolutionReportAction extends GenericReportAction {
    private BusinessUnit businessUnit;
    private ProductItem productItem;
    private Warehouse warehouse;
    private BigDecimal initUnitaryBalance;
    private BigDecimal endUnitaryBalance;
    private Month month;
    private Date initDate;
    private Date endDate;

    @Create
    public void init() {
        restrictions = new String[]{
                "executorUnit=#{generalValuedEvolutionReportAction.businessUnit}",
                "warehouse=#{generalValuedEvolutionReportAction.warehouse}",
                "productItem=#{generalValuedEvolutionReportAction.productItem}",
                "inventory.unitaryBalance >= #{generalValuedEvolutionReportAction.initUnitaryBalance}",
                "inventory.unitaryBalance <= #{generalValuedEvolutionReportAction.endUnitaryBalance}"
        };
        sortProperty = "groupItem.id, subGroupItem.id, warehouse.id.warehouseCode, warehouse.id.companyNumber, productItem.productItemCode";
    }

    @Override
    protected String getEjbql() {
        return "SELECT warehouse.id, " +
                "      warehouse.name , " +
                "      productItem.id, " +
                "      productItem.productItemCode, " +
                "      productItem.name, " +
                "      productItem.usageMeasureUnit, " +
                "      productItem.unitCost, " +
                "      inventory.unitaryBalance, " +
                "      subGroupItem, " +
                "      groupItem " +//'item' postfix is because 'group' is a reserved word and can't be used as alias
                "FROM  ProductItem productItem " +
                "      JOIN productItem.inventories inventory " +
                "      LEFT JOIN inventory.warehouse warehouse " +
                "      LEFT JOIN productItem.subGroup subGroupItem " +
                "      LEFT JOIN subGroupItem.group groupItem " +
                "      LEFT JOIN warehouse.executorUnit executorUnit";

    }

    public void generateReport() {
        log.debug("Generating general valued evolution Report ...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "generalValuedEvolutionReport",
                "/warehouse/reports/generalValuedEvolutionReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("GeneralValuedEvolutionReport.report.page.title"),
                reportParameters);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void clearWarehouse() {
        setWarehouse(null);
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BigDecimal getInitUnitaryBalance() {
        return initUnitaryBalance;
    }

    public void setInitUnitaryBalance(BigDecimal initUnitaryBalance) {
        this.initUnitaryBalance = initUnitaryBalance;
    }

    public BigDecimal getEndUnitaryBalance() {
        return endUnitaryBalance;
    }

    public void setEndUnitaryBalance(BigDecimal endUnitaryBalance) {
        this.endUnitaryBalance = endUnitaryBalance;
    }

    @Factory(value = "monthList", scope = ScopeType.STATELESS)
    public Month[] getMonthList() {
        return Month.values();
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setDates() {
        int currentYear = new GregorianCalendar().get(Calendar.YEAR);
        endDate = DateUtils.lastDayOfMonth(getMonth().getValue(), currentYear);
        initDate = DateUtils.firstDayOfMonth(getMonth().getValue(), currentYear);
    }
}

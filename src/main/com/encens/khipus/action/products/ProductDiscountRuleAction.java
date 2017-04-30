package com.encens.khipus.action.products;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;
import com.encens.khipus.service.products.ProductDiscountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.security.Restrict;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actions for Product Discount Rules
 *
 * @author:
 */

@Name("productDiscountRuleAction")
@Scope(ScopeType.CONVERSATION)
public class ProductDiscountRuleAction extends GenericAction<ProductDiscountRule> {

    private boolean selectAllOption;
    private String searchName;
    private String searchCode;

    @In(required = false)
    private User currentUser;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private ProductDiscountService productDiscountService;

    private Map<Product, Boolean> selectedProducts = new HashMap<Product, Boolean>();

    @DataModel
    public List<Product> getProductList() {
        return getQueryResult();
    }

    @Factory(value = "productDiscountRule", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PRODUCTDISCOUNTRULE','VIEW')}")
    public ProductDiscountRule initProductDiscountRule() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PRODUCTDISCOUNTRULE','CREATE')}")
    public String create() {
        try {
            getInstance().setUser(currentUser);
            genericService.create(getInstance());
            addCreatedMessage();
            return select(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict("#{s:hasPermission('PRODUCTDISCOUNTRULE','UPDATE')}")
    public String updateRule() {
        getInstance().setUser(currentUser);
        return update();
    }

    @SuppressWarnings({"unchecked"})
    public List<Product> getQueryResult() {
        try {
            String namePattern = getSearchPattern(searchName);
            String codePattern = getSearchPattern(searchCode);
            Query query = em.createQuery("select product from Product product where lower(product.name) " +
                    "like :namePattern and lower(product.code) like :codePattern order by product.name ");
            query.setParameter("namePattern", namePattern);
            query.setParameter("codePattern", codePattern);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    private String getSearchPattern(String criteria) {
        return criteria == null ? "%" : criteria.toLowerCase().replace('*', '%') + '%';
    }

    public void selectProduct(Product product) {
        System.out.println("***********" + selectedProducts.get(product));
        if (selectedProducts.get(product)) {
            getInstance().getProducts().add(product);
        } else {
            selectAllOption = false;
            getInstance().getProducts().remove(product);
        }
        update();
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchCode() {
        return searchCode;
    }

    public void setSearchCode(String searchCode) {
        this.searchCode = searchCode;
    }

    public Map<Product, Boolean> getSelectedProducts() {
        if (selectedProducts.isEmpty()) {
            for (Product product : getQueryResult()) {
                if (productDiscountService.findDiscountByRule(product, getInstance()) != null) {
                    selectedProducts.put(product, Boolean.TRUE);
                } else {
                    selectedProducts.put(product, Boolean.FALSE);
                }
            }
        }
        return selectedProducts;
    }

    public void setSelectedProducts(Map<Product, Boolean> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public boolean isSelectAllOption() {
        return selectAllOption;
    }

    public void setSelectAllOption(boolean selectAllOption) {
        this.selectAllOption = selectAllOption;
    }

    public void selectAllAction() {
        if (selectAllOption) {
            for (Product product : getQueryResult()) {
                selectedProducts.put(product, Boolean.TRUE);
            }
        }
    }

    public void assignCustomers() {
        for (Product product : getQueryResult()) {
            if (selectedProducts.get(product)) {
                productDiscountService.newDiscount(getInstance(), product);
            }
        }
        update();
    }

    public void cancelAction() {
        selectAllOption = false;
        selectedProducts = new HashMap<Product, Boolean>();
    }

    @Override
    @Restrict("#{s:hasPermission('PRODUCTDISCOUNTRULE','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            genericService.update(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(genericService.findById(getEntityClass(), getId(getInstance())));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }
}

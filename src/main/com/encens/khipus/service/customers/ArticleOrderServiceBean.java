package com.encens.khipus.service.customers;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.warehouse.WarehouseVoucherCreateAction;
import com.encens.khipus.action.warehouse.WarehouseVoucherGeneralAction;
import com.encens.khipus.action.warehouse.WarehouseVoucherUpdateAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.service.warehouse.ApprovalWarehouseVoucherService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.query.QueryUtils;
import com.encens.khipus.util.warehouse.WarehouseUtil;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.*;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("articleOrderService")
@AutoCreate
public class ArticleOrderServiceBean extends GenericServiceBean implements ArticleOrderService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @SuppressWarnings(value = "unchecked")
    public List<ArticleOrder> findCashSaleDetailByCodeAndDate(ProductItem productItem, Date startDate, Date endDate) {
        return em.createNamedQuery("ArticleOrder.findCashSaleDetailByCodeAndDate")
                .setParameter("productItemCode", productItem.getProductItemCode())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ArticleOrder> findOrderDetailByCodeAndDate(ProductItem productItem, Date startDate, Date endDate) {
        return em.createNamedQuery("ArticleOrder.findOrderDetailByCodeAndDate")
                .setParameter("productItemCode", productItem.getProductItemCode())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}

package com.encens.khipus.test.customers;

import com.encens.khipus.service.customers.SaleTransactionServiceBean;
import com.encens.khipus.service.finances.SaleSequenceServiceBean;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.lang.reflect.Method;

/**
 * Verifica que las anotaciones de transaccion en los nuevos servicios
 * esten correctamente configuradas para resolver el bug de concurrencia.
 */
public class SaleServiceAnnotationTest {

    @Test
    public void testSequenceGeneratorIsStateless() {
        Assert.assertTrue(
                SaleSequenceServiceBean.class.isAnnotationPresent(Stateless.class),
                "SaleSequenceServiceBean debe ser @Stateless");
        System.out.println("[PASS] SaleSequenceServiceBean es @Stateless");
    }

    @Test
    public void testSequenceGeneratorGetNextValueRequiresNew() throws Exception {
        Method method = SaleSequenceServiceBean.class.getMethod("getNextValue", String.class);
        TransactionAttribute ta = method.getAnnotation(TransactionAttribute.class);
        Assert.assertNotNull(ta,
                "getNextValue() debe tener @TransactionAttribute");
        Assert.assertEquals(ta.value(), TransactionAttributeType.REQUIRES_NEW,
                "getNextValue() debe usar REQUIRES_NEW para liberar lock de secuencia inmediatamente");
        System.out.println("[PASS] getNextValue() tiene @TransactionAttribute(REQUIRES_NEW)");
    }

    @Test
    public void testSaleTransactionIsStateless() {
        Assert.assertTrue(
                SaleTransactionServiceBean.class.isAnnotationPresent(Stateless.class),
                "SaleTransactionServiceBean debe ser @Stateless");
        System.out.println("[PASS] SaleTransactionServiceBean es @Stateless");
    }

    @Test
    public void testCreateSaleWithInventoryRequiresNew() throws Exception {
        Method method = SaleTransactionServiceBean.class.getMethod(
                "createSaleWithInventory",
                com.encens.khipus.model.customers.CustomerOrder.class);
        TransactionAttribute ta = method.getAnnotation(TransactionAttribute.class);
        Assert.assertNotNull(ta,
                "createSaleWithInventory() debe tener @TransactionAttribute");
        Assert.assertEquals(ta.value(), TransactionAttributeType.REQUIRES_NEW,
                "createSaleWithInventory() debe usar REQUIRES_NEW para transaccion atomica independiente");
        System.out.println("[PASS] createSaleWithInventory() tiene @TransactionAttribute(REQUIRES_NEW)");
    }

    @Test
    public void testSaleTransactionImplementsInterface() {
        Assert.assertTrue(
                com.encens.khipus.service.customers.SaleTransactionService.class
                        .isAssignableFrom(SaleTransactionServiceBean.class),
                "SaleTransactionServiceBean debe implementar SaleTransactionService");
        System.out.println("[PASS] SaleTransactionServiceBean implementa SaleTransactionService");
    }

    @Test
    public void testSequenceGeneratorImplementsInterface() {
        Assert.assertTrue(
                com.encens.khipus.service.finances.SaleSequenceService.class
                        .isAssignableFrom(SaleSequenceServiceBean.class),
                "SaleSequenceServiceBean debe implementar SequenceGeneratorService");
        System.out.println("[PASS] SaleSequenceServiceBean implementa SequenceGeneratorService");
    }
}

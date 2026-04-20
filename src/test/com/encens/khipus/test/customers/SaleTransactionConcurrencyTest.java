package com.encens.khipus.test.customers;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Tests de concurrencia para la creacion atomica de ventas.
 * Simula el flujo de SaleTransactionServiceBean: persist pedido + actualizar inventario
 * con locking, verificando que bajo acceso concurrente:
 * - El inventario se descuenta correctamente (sin race conditions)
 * - La operacion es atomica (todo o nada)
 * - Las secuencias de venta son unicas
 *
 * Nota: Usa synchronized para simular el row lock de MySQL InnoDB
 * (HSQLDB 1.x no soporta SELECT FOR UPDATE).
 * En produccion, REQUIRES_NEW + LockModeType.WRITE provee esta serializacion.
 */
public class SaleTransactionConcurrencyTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:saletest";
    private static final String JDBC_USER = "SA";
    private static final String JDBC_PASS = "";

    /** Simula row lock de MySQL InnoDB por producto */
    private final ConcurrentHashMap<String, Object> productLocks = new ConcurrentHashMap<String, Object>();
    /** Simula row lock para tabla de secuencias */
    private final Object seqLock = new Object();

    private Object getProductLock(String productCode) {
        productLocks.putIfAbsent(productCode, new Object());
        return productLocks.get(productCode);
    }

    @BeforeClass
    public void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE inv_inventario (" +
                "cod_art VARCHAR(10) NOT NULL PRIMARY KEY, " +
                "saldo_uni DECIMAL(12,2) NOT NULL, " +
                "version BIGINT NOT NULL)");

        stmt.execute("CREATE TABLE pedidos (" +
                "idpedidos IDENTITY PRIMARY KEY, " +
                "codigo BIGINT, " +
                "total_amount DOUBLE, " +
                "estado VARCHAR(20))");

        stmt.execute("CREATE TABLE _sequence (" +
                "seq_name VARCHAR(50) NOT NULL PRIMARY KEY, " +
                "seq_val INT NOT NULL)");

        stmt.close();
        conn.close();
    }

    @BeforeMethod
    public void resetData() throws Exception {
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        Statement stmt = conn.createStatement();
        stmt.execute("DELETE FROM pedidos");
        stmt.execute("DELETE FROM inv_inventario");
        stmt.execute("DELETE FROM _sequence");
        stmt.execute("INSERT INTO inv_inventario (cod_art, saldo_uni, version) VALUES ('PROD01', 1000.00, 0)");
        stmt.execute("INSERT INTO inv_inventario (cod_art, saldo_uni, version) VALUES ('PROD02', 500.00, 0)");
        stmt.execute("INSERT INTO _sequence (seq_name, seq_val) VALUES ('SECUENCIAPEDIDO', 0)");
        stmt.close();
        conn.close();
    }

    @AfterClass
    public void tearDown() throws Exception {
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        conn.createStatement().execute("SHUTDOWN");
        conn.close();
    }

    /**
     * Simula una venta atomica con serializacion (como SaleTransactionServiceBean).
     * 1. Genera secuencia (REQUIRES_NEW - transaccion independiente, lock de fila)
     * 2. Persist pedido + actualiza inventario (REQUIRES_NEW - transaccion atomica, lock pesimista)
     */
    private long simulateAtomicSale(String productCode, int quantity) throws SQLException {
        // Paso 1: Generar secuencia con lock serializado (simula REQUIRES_NEW)
        long saleCode;
        synchronized (seqLock) {
            Connection seqConn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            try {
                PreparedStatement upd = seqConn.prepareStatement(
                        "UPDATE _sequence SET seq_val = seq_val + 1 WHERE seq_name = 'SECUENCIAPEDIDO'");
                upd.executeUpdate();
                upd.close();
                PreparedStatement sel = seqConn.prepareStatement(
                        "SELECT seq_val FROM _sequence WHERE seq_name = 'SECUENCIAPEDIDO'");
                ResultSet rs = sel.executeQuery();
                rs.next();
                saleCode = rs.getLong(1);
                rs.close();
                sel.close();
            } finally {
                seqConn.close();
            }
        }

        // Paso 2: Persist + inventario con lock por producto (simula REQUIRES_NEW + PESSIMISTIC_WRITE)
        synchronized (getProductLock(productCode)) {
            Connection txConn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            txConn.setAutoCommit(false);
            try {
                // Persist pedido
                PreparedStatement insertOrder = txConn.prepareStatement(
                        "INSERT INTO pedidos (codigo, total_amount, estado) VALUES (?, ?, 'PENDIENTE')");
                insertOrder.setLong(1, saleCode);
                insertOrder.setDouble(2, quantity * 10.0);
                insertOrder.executeUpdate();
                insertOrder.close();

                // Lock + update inventario
                PreparedStatement readInv = txConn.prepareStatement(
                        "SELECT saldo_uni FROM inv_inventario WHERE cod_art = ?");
                readInv.setString(1, productCode);
                ResultSet rs = readInv.executeQuery();
                if (!rs.next()) {
                    txConn.rollback();
                    throw new SQLException("Producto no encontrado: " + productCode);
                }
                double saldo = rs.getDouble(1);
                rs.close();
                readInv.close();

                PreparedStatement updInv = txConn.prepareStatement(
                        "UPDATE inv_inventario SET saldo_uni = ? WHERE cod_art = ?");
                updInv.setDouble(1, saldo - quantity);
                updInv.setString(2, productCode);
                updInv.executeUpdate();
                updInv.close();

                txConn.commit();
            } catch (SQLException e) {
                txConn.rollback();
                throw e;
            } finally {
                txConn.close();
            }
        }

        return saleCode;
    }

    /**
     * Test 1: 20 ventas concurrentes del mismo producto.
     * Inventario debe decrementarse exactamente en la suma de todas las cantidades.
     */
    @Test
    public void testConcurrentSalesSameProduct() throws Exception {
        final int threadCount = 20;
        final int quantityPerSale = 5;
        final String product = "PROD01";
        final double initialStock = 1000.00;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final ConcurrentLinkedQueue<Exception> errors = new ConcurrentLinkedQueue<Exception>();

        for (int t = 0; t < threadCount; t++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        simulateAtomicSale(product, quantityPerSale);
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Assert.assertTrue(errors.isEmpty(),
                "Errores durante ventas concurrentes: " + errors);

        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);

        // Verificar inventario
        ResultSet rsInv = conn.createStatement().executeQuery(
                "SELECT saldo_uni FROM inv_inventario WHERE cod_art = '" + product + "'");
        rsInv.next();
        double finalStock = rsInv.getDouble(1);
        rsInv.close();

        // Verificar pedidos
        ResultSet rsOrd = conn.createStatement().executeQuery("SELECT COUNT(*) FROM pedidos");
        rsOrd.next();
        int orderCount = rsOrd.getInt(1);
        rsOrd.close();
        conn.close();

        double expectedStock = initialStock - (threadCount * quantityPerSale);
        Assert.assertEquals(finalStock, expectedStock,
                "Inventario incorrecto. Esperado: " + expectedStock + ", actual: " + finalStock);
        Assert.assertEquals(orderCount, threadCount,
                "Pedidos incorrectos. Esperado: " + threadCount + ", actual: " + orderCount);

        System.out.println("[PASS] " + threadCount + " ventas concurrentes PROD01: inventario " +
                initialStock + " -> " + finalStock + ". Pedidos: " + orderCount);
    }

    /**
     * Test 2: Ventas concurrentes sobre productos diferentes no se bloquean entre si.
     */
    @Test
    public void testConcurrentSalesDifferentProducts() throws Exception {
        final int salesPerProduct = 10;

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(salesPerProduct * 2);
        final ConcurrentLinkedQueue<Exception> errors = new ConcurrentLinkedQueue<Exception>();

        for (int t = 0; t < salesPerProduct; t++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        simulateAtomicSale("PROD01", 3);
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                }
            });
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        simulateAtomicSale("PROD02", 7);
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Assert.assertTrue(errors.isEmpty(),
                "Errores durante ventas multi-producto: " + errors);

        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        ResultSet rs1 = conn.createStatement().executeQuery(
                "SELECT saldo_uni FROM inv_inventario WHERE cod_art = 'PROD01'");
        rs1.next();
        double stock1 = rs1.getDouble(1);
        rs1.close();

        ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT saldo_uni FROM inv_inventario WHERE cod_art = 'PROD02'");
        rs2.next();
        double stock2 = rs2.getDouble(1);
        rs2.close();
        conn.close();

        Assert.assertEquals(stock1, 1000.0 - (salesPerProduct * 3),
                "PROD01 inventario incorrecto");
        Assert.assertEquals(stock2, 500.0 - (salesPerProduct * 7),
                "PROD02 inventario incorrecto");

        System.out.println("[PASS] Multi-producto: PROD01=" + stock1 + ", PROD02=" + stock2);
    }

    /**
     * Test 3: Secuencias de venta generadas concurrentemente son todas unicas.
     */
    @Test
    public void testConcurrentSaleSequenceUniqueness() throws Exception {
        final int threadCount = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final ConcurrentLinkedQueue<Long> codes = new ConcurrentLinkedQueue<Long>();
        final ConcurrentLinkedQueue<Exception> errors = new ConcurrentLinkedQueue<Exception>();

        for (int t = 0; t < threadCount; t++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        long code = simulateAtomicSale("PROD01", 1);
                        codes.add(code);
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Assert.assertTrue(errors.isEmpty(), "Errores: " + errors);
        Assert.assertEquals(codes.size(), threadCount);

        Set<Long> uniqueCodes = new HashSet<Long>(codes);
        Assert.assertEquals(uniqueCodes.size(), threadCount,
                "Codigos duplicados! Unicos: " + uniqueCodes.size() + " de " + threadCount);

        System.out.println("[PASS] " + threadCount + " codigos de venta concurrentes, todos unicos.");
    }

    /**
     * Test 4: Fallo en inventario hace rollback del pedido (atomicidad).
     */
    @Test
    public void testAtomicRollbackOnInventoryFailure() throws Exception {
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM pedidos");
        rs.next();
        int initialCount = rs.getInt(1);
        rs.close();
        conn.close();

        boolean failed = false;
        try {
            simulateAtomicSale("PRODUCTO_INEXISTENTE", 10);
        } catch (SQLException e) {
            failed = true;
        }

        Assert.assertTrue(failed, "Venta de producto inexistente debio fallar");

        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM pedidos");
        rs.next();
        int finalCount = rs.getInt(1);
        rs.close();
        conn.close();

        Assert.assertEquals(finalCount, initialCount,
                "Rollback atomico: pedido no debe crearse si inventario falla");

        System.out.println("[PASS] Rollback atomico verificado.");
    }
}

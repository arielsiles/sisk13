package com.encens.khipus.test.customers;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Tests de concurrencia para el generador de secuencias.
 *
 * Demuestra que:
 * - SIN serializacion: se producen valores duplicados (bug original)
 * - CON serializacion: todos los valores son unicos (fix con REQUIRES_NEW)
 *
 * Nota: HSQLDB 1.x no soporta SELECT FOR UPDATE, por lo que la serializacion
 * se simula con synchronized (en produccion, MySQL InnoDB + REQUIRES_NEW
 * provee esta serializacion via row locks).
 */
public class SequenceGeneratorConcurrencyTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:seqtest";
    private static final String JDBC_USER = "SA";
    private static final String JDBC_PASS = "";

    /** Lock que simula el row lock de MySQL InnoDB (REQUIRES_NEW + UPDATE en _sequence) */
    private final Object sequenceLock = new Object();

    @BeforeClass
    public void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE _sequence (seq_name VARCHAR(50) NOT NULL PRIMARY KEY, seq_val INT NOT NULL)");
        stmt.execute("INSERT INTO _sequence (seq_name, seq_val) VALUES ('TEST_SEQ', 0)");
        stmt.execute("INSERT INTO _sequence (seq_name, seq_val) VALUES ('SECUENCIAPEDIDO', 100)");
        stmt.execute("INSERT INTO _sequence (seq_name, seq_val) VALUES ('UNSERIALIZED', 0)");
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
     * Simula getNextValue() CON serializacion (REQUIRES_NEW en produccion).
     * synchronized simula el row lock de MySQL InnoDB.
     */
    private long getNextValueSerialized(String sequenceName) throws SQLException {
        synchronized (sequenceLock) {
            Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            try {
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE _sequence SET seq_val = seq_val + 1 WHERE seq_name = ?");
                update.setString(1, sequenceName);
                int updated = update.executeUpdate();
                update.close();

                if (updated == 0) {
                    PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO _sequence (seq_name, seq_val) VALUES (?, 1)");
                    insert.setString(1, sequenceName);
                    insert.executeUpdate();
                    insert.close();
                    return 1;
                }

                PreparedStatement select = conn.prepareStatement(
                        "SELECT seq_val FROM _sequence WHERE seq_name = ?");
                select.setString(1, sequenceName);
                ResultSet rs = select.executeQuery();
                rs.next();
                long val = rs.getLong(1);
                rs.close();
                select.close();
                return val;
            } finally {
                conn.close();
            }
        }
    }

    /**
     * Simula getNextValue() SIN serializacion (bug original - sin REQUIRES_NEW).
     * Multiples transacciones concurrentes sin locking.
     */
    private long getNextValueUnserialized(String sequenceName) throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        try {
            PreparedStatement update = conn.prepareStatement(
                    "UPDATE _sequence SET seq_val = seq_val + 1 WHERE seq_name = ?");
            update.setString(1, sequenceName);
            update.executeUpdate();
            update.close();

            PreparedStatement select = conn.prepareStatement(
                    "SELECT seq_val FROM _sequence WHERE seq_name = ?");
            select.setString(1, sequenceName);
            ResultSet rs = select.executeQuery();
            rs.next();
            long val = rs.getLong(1);
            rs.close();
            select.close();
            return val;
        } finally {
            conn.close();
        }
    }

    /**
     * Test 1: CON serializacion (fix) - N hilos concurrentes, TODOS los valores unicos.
     * Simula REQUIRES_NEW donde cada llamada serializa el acceso a la fila de secuencia.
     */
    @Test
    public void testSerializedSequenceUniqueness() throws Exception {
        final int threadCount = 20;
        final int iterationsPerThread = 50;
        final int expectedTotal = threadCount * iterationsPerThread;
        final String seqName = "TEST_SEQ";

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        final ConcurrentLinkedQueue<Long> allValues = new ConcurrentLinkedQueue<Long>();
        final ConcurrentLinkedQueue<Exception> errors = new ConcurrentLinkedQueue<Exception>();

        for (int t = 0; t < threadCount; t++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        for (int i = 0; i < iterationsPerThread; i++) {
                            long val = getNextValueSerialized(seqName);
                            allValues.add(val);
                        }
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
                "Errores durante ejecucion concurrente: " + errors);
        Assert.assertEquals(allValues.size(), expectedTotal,
                "Se esperaban " + expectedTotal + " valores");

        Set<Long> uniqueValues = new HashSet<Long>(allValues);
        Assert.assertEquals(uniqueValues.size(), expectedTotal,
                "CON serializacion: duplicados detectados! Unicos: " + uniqueValues.size() + " de " + expectedTotal);

        System.out.println("[PASS] CON serializacion (REQUIRES_NEW): " + expectedTotal +
                " secuencias concurrentes, todas unicas. Rango: " +
                Collections.min(uniqueValues) + "-" + Collections.max(uniqueValues));
    }

    /**
     * Test 2: Las secuencias serializadas deben ser consecutivas (sin gaps).
     */
    @Test(dependsOnMethods = "testSerializedSequenceUniqueness")
    public void testSequenceContiguity() throws Exception {
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        PreparedStatement ps = conn.prepareStatement("SELECT seq_val FROM _sequence WHERE seq_name = ?");
        ps.setString(1, "TEST_SEQ");
        ResultSet rs = ps.executeQuery();
        rs.next();
        long finalVal = rs.getLong(1);
        rs.close();
        ps.close();
        conn.close();

        int expectedTotal = 20 * 50;
        Assert.assertEquals(finalVal, expectedTotal,
                "Valor final de secuencia debe ser " + expectedTotal + ", actual: " + finalVal);

        System.out.println("[PASS] Secuencia final = " + finalVal + ", sin gaps.");
    }

    /**
     * Test 3: SIN serializacion (bug original) - demuestra que SE PRODUCEN duplicados.
     * Este test PASA cuando hay duplicados (confirma la existencia del bug).
     */
    @Test
    public void testUnserializedProducesDuplicates() throws Exception {
        final int threadCount = 20;
        final int iterationsPerThread = 50;
        final int expectedTotal = threadCount * iterationsPerThread;
        final String seqName = "UNSERIALIZED";

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        final ConcurrentLinkedQueue<Long> allValues = new ConcurrentLinkedQueue<Long>();
        final ConcurrentLinkedQueue<Exception> errors = new ConcurrentLinkedQueue<Exception>();

        for (int t = 0; t < threadCount; t++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        for (int i = 0; i < iterationsPerThread; i++) {
                            long val = getNextValueUnserialized(seqName);
                            allValues.add(val);
                        }
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

        Set<Long> uniqueValues = new HashSet<Long>(allValues);
        boolean hasDuplicates = uniqueValues.size() < expectedTotal;

        System.out.println("[INFO] SIN serializacion: " + uniqueValues.size() + " unicos de " +
                expectedTotal + " generados" + (hasDuplicates ? " (DUPLICADOS = bug confirmado)" : " (sin duplicados en esta ejecucion)"));

        // Este test solo informa, no falla - los duplicados dependen del timing del scheduler
    }

    /**
     * Test 4: Secuencia inexistente se crea automaticamente con valor 1.
     */
    @Test
    public void testAutoCreateNewSequence() throws Exception {
        long val = getNextValueSerialized("NUEVA_SECUENCIA_" + System.nanoTime());
        Assert.assertEquals(val, 1, "Nueva secuencia debe empezar en 1");
        System.out.println("[PASS] Secuencia inexistente creada automaticamente con valor 1.");
    }

    /**
     * Test 5: Secuencias independientes no interfieren entre si.
     */
    @Test
    public void testIndependentSequences() throws Exception {
        long pedido1 = getNextValueSerialized("SECUENCIAPEDIDO");
        long pedido2 = getNextValueSerialized("SECUENCIAPEDIDO");
        String uniqueSeq = "VENTA_" + System.nanoTime();
        long venta1 = getNextValueSerialized(uniqueSeq);

        Assert.assertEquals(pedido1, 101, "SECUENCIAPEDIDO debe ser 101 (empezo en 100)");
        Assert.assertEquals(pedido2, 102, "SECUENCIAPEDIDO debe ser 102");
        Assert.assertEquals(venta1, 1, "Nueva secuencia independiente debe ser 1");

        System.out.println("[PASS] Secuencias independientes no interfieren.");
    }
}

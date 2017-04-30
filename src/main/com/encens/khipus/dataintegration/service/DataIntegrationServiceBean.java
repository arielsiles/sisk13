package com.encens.khipus.dataintegration.service;

import com.encens.khipus.dataintegration.configuration.structure.Column;
import com.encens.khipus.dataintegration.configuration.structure.IntegrationElement;
import com.encens.khipus.dataintegration.util.DataIntegrationException;
import com.encens.khipus.dataintegration.util.SQLUtil;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.2.10
 */
@Stateless
@TransactionManagement(javax.ejb.TransactionManagementType.BEAN)
@Name("dataIntegrationService")
public class DataIntegrationServiceBean implements DataIntegrationService {


    private static final LogProvider log = Logging.getLogProvider(DataIntegrationServiceBean.class);
    private static final int MAX_ROWS = 100;


    @Resource
    protected EJBContext ejbContext;


    /**
     * Execute the main logic for dataintegration.
     * <p/>
     * - Obtain the local and integration <code>DataSource</code> object.
     * - Read the information to be integrated.
     * - Build the insert and delete statements.
     * - Execute data integration logic.
     * - Execute insters and delete statements.
     *
     * @param localDataSource    <code>String</code> that contain the JNDI name for local <code>DataSource</code>.
     * @param integrationElement <code>IntegrationElement</code> object.
     */
    public void executeIntegration(String localDataSource, IntegrationElement integrationElement) {

        DataSource integrationDataSource = getDataSource(integrationElement.getDataSource());
        if (null == integrationDataSource) {
            return;
        }

        DataSource khipusDatasource = getDataSource(localDataSource);
        if (null == khipusDatasource) {
            return;
        }

        String localIdentifierQuote;
        List<Map<String, String>> sourceData;

        try {
            sourceData = readSourceTable(integrationElement, integrationDataSource);
            localIdentifierQuote = getIdentifierQuoteString(khipusDatasource);
        } catch (DataIntegrationException e) {
            log.error("Cannot read source information to integrate " + integrationElement.getApplicationId() + ".", e);
            return;
        }

        if (!ValidatorUtil.isEmptyOrNull(sourceData)) {
            int iterations = sourceData.size();
            for (int i = 0; i < iterations; i++) {
                List<Map<String, String>> sourceDataSubList = sourceData.subList(i, i + 1);
                if (!ValidatorUtil.isEmptyOrNull(sourceDataSubList)) {
                    List<String> insertSQLs = buildInsertSQLs(integrationElement, sourceDataSubList, localIdentifierQuote);
                    String deleteSQLs = buildDeleteSQLs(integrationElement, sourceDataSubList);
                    UserTransaction userTransaction = ejbContext.getUserTransaction();
                    try {
                        userTransaction.setTransactionTimeout(sourceDataSubList.size() * 60);
                        userTransaction.begin();
                        try {
                            for (Map<String, String> row : sourceDataSubList) {
                                customOperations(userTransaction, integrationElement, row);
                            }
                            postIterateRows();
                            writeTargetTable(khipusDatasource, insertSQLs);
                            writeSourceTable(integrationDataSource, deleteSQLs);
                        } catch (Exception e) {
                            userTransaction.rollback();
                            throw new RuntimeException("Cannot integrate data from " +
                                    integrationElement.getApplicationId() + " application.", e);
                        }
                        userTransaction.commit();
                        userTransaction.setTransactionTimeout(0);
                    } catch (Exception e) {
                        log.error("Unexpected error has happened", e);
                    }
                }
            }
        }
    }

    public void customOperations(UserTransaction userTransaction,
                                 IntegrationElement integrationElement,
                                 Map<String, String> row) throws CompanyConfigurationNotFoundException {
        log.debug("DataIntegrationService.cutomOperations()");
    }

    public void postIterateRows() throws CompanyConfigurationNotFoundException {
        log.debug("DataIntegrationService.postIterateRows()");
    }

    protected DataSource getDataSource(String dataSourceJNDI) {
        try {
            Context context = new javax.naming.InitialContext();

            return (DataSource) context.lookup(dataSourceJNDI);
        } catch (NamingException e) {
            log.error("Cannot find the dataSource " + dataSourceJNDI);
        }

        return null;
    }

    /**
     * Read the information to be integrated.
     *
     * @param integrationElement    <code>IntegrationElement</code> object.
     * @param integrationDataSource <code>DataSource</code> object.
     * @return a <code>List</code> of <code>Map</code> object contain all information to be integrated.
     * @throws DataIntegrationException if cannot recover the information.
     */
    protected List<Map<String, String>> readSourceTable(
            IntegrationElement integrationElement, DataSource integrationDataSource) throws DataIntegrationException {

        List<Map<String, String>> sourceData = new ArrayList<Map<String, String>>();

        String sql = SQLUtil.i.generateSelectSQL(integrationElement.getSourceTable());

        Connection connection = openConnection(integrationDataSource);
        try {
            Statement statement = connection.createStatement();
            statement.setMaxRows(MAX_ROWS);
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Map<String, String> data = new HashMap<String, String>();

                for (Column column : integrationElement.getSourceTable().getColumns()) {
                    String value = resultSet.getString(column.getAlias());
                    data.put(column.getAlias(), value);
                }
                sourceData.add(data);
            }
        } catch (SQLException e) {
            throw new DataIntegrationException("Cannot execute the " + sql + ".", e);
        } finally {
            closeConnection(connection);
        }

        return sourceData;
    }

    protected Connection openConnection(DataSource dataSource) throws DataIntegrationException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataIntegrationException("Cannot stablish the database connection.", e);
        }
    }

    protected void closeConnection(Connection connection) {
        if (null != connection) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Execute insert sql statements in local <code>DataSource</code>.
     *
     * @param localDataSource <code>DataSource</code> object, its the local <code>DataSource</code>.
     * @param insertSQLs      <code>List</code> of <code>String</code> object that contain the insert statements.
     * @throws DataIntegrationException If cannot execute some statement or cannot open the jdbc connection
     *                                  assiciated to local <code>DataSource</code>.
     */
    protected void writeTargetTable(DataSource localDataSource,
                                    List<String> insertSQLs) throws DataIntegrationException {
        Connection connection = openConnection(localDataSource);

        try {
            Statement statement = connection.createStatement();
            for (String sql : insertSQLs) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new DataIntegrationException("Cannot execute the insert sql.", e);
        } finally {
            closeConnection(connection);
        }
    }


    /**
     * Execute delete statements in the integration <code>DataSource</code>
     *
     * @param integrationDataSource <code>DataSource</code> object.
     * @param deleteSQLs            <code>String</code> object that contain the delete statement.
     * @throws DataIntegrationException If cannot execute some statement or cannot open the jdbc connection
     *                                  assiciated to local <code>DataSource</code>.
     */
    protected void writeSourceTable(DataSource integrationDataSource,
                                    String deleteSQLs) throws DataIntegrationException {
        Connection connection = openConnection(integrationDataSource);

        try {
            connection.createStatement().executeUpdate(deleteSQLs);
        } catch (SQLException e) {
            throw new DataIntegrationException("Cannot execute the " + deleteSQLs + ".", e);
        } finally {
            closeConnection(connection);
        }
    }

    protected List<String> buildInsertSQLs(IntegrationElement integrationElement, List<Map<String, String>> sourceData,
                                           String identifierQuote) {
        return SQLUtil.i.generateInsertSQL(integrationElement.getTargetTable(), sourceData, identifierQuote);
    }

    protected String buildDeleteSQLs(IntegrationElement integrateElement, List<Map<String, String>> sourceData) {
        return SQLUtil.i.generateDeleteSQL(integrateElement.getSourceTable(), sourceData);
    }

    private String getIdentifierQuoteString(DataSource dataSource) throws DataIntegrationException {

        String identifierQuoteString;

        Connection connection = openConnection(dataSource);

        DatabaseMetaData dbmdt;
        try {
            dbmdt = connection.getMetaData();
            identifierQuoteString = dbmdt.getIdentifierQuoteString();
        } catch (SQLException e) {
            throw new DataIntegrationException("Cannot the IdentifierQuoteString ", e);
        }

        closeConnection(connection);

        return identifierQuoteString;
    }
}

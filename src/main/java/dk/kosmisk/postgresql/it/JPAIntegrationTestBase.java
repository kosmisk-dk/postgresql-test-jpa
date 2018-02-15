package dk.kosmisk.postgresql.it;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JTA_DATASOURCE;
import static org.hibernate.cfg.AvailableSettings.DATASOURCE;

/**
 *
 *
 * @author Source (source (at) kosmisk.dk)
 *
 */
public class JPAIntegrationTestBase {

    protected final PostgresITDataSource dataSource;
    protected final EntityManager entityManager;

    private final HashMap<String, Object> entityManagerProps;
    private final EntityManagerFactory entityManagerFactiory;

    /**
     * Build a JPA Integraion Test Environment
     *
     * @param persistenceUnit name of persistence unit
     * @param logLevel        eclipselink logging level
     * @param dataSource      PostgreSQL datasource
     */
    public JPAIntegrationTestBase(String persistenceUnit, String logLevel, PostgresITDataSource dataSource) {
        this.dataSource = dataSource;
        this.entityManagerProps = new HashMap<>();
        this.entityManagerProps.put("eclipselink.logging.level", logLevel.toUpperCase(Locale.ROOT));
        /*
         * Knowledge gained from
         * https://stackoverflow.com/questions/26173083/how-to-use-jpa-with-a-random-database-i-e-user-submitted
         */
        this.entityManagerProps.put(JTA_DATASOURCE, dataSource);
        this.entityManagerProps.put(DATASOURCE, dataSource);
        this.entityManagerFactiory = Persistence.createEntityManagerFactory(persistenceUnit, this.entityManagerProps);
        this.entityManager = entityManagerFactiory.createEntityManager();
    }

    /**
     * Build a JPA Integraion Test Environment
     *
     * @param persistenceUnit name of persistence unit
     * @param dataSource      PostgreSQL datasource
     */
    public JPAIntegrationTestBase(String persistenceUnit, PostgresITDataSource dataSource) {
        this(persistenceUnit, "FINE", dataSource);
    }

    /**
     * Build a JPA Integraion Test Environment
     *
     * @param persistenceUnit name of persistence unit
     * @param logLevel        eclipselink logging level
     * @param databaseName    used by {@link PostgresITDataSource.Builder#fromProperty(java.lang.String)
     *                        }
     */
    public JPAIntegrationTestBase(String persistenceUnit, String logLevel, String databaseName) {
        this(persistenceUnit, logLevel,
             PostgresITDataSource.builder()
                     .fromProperty(databaseName)
                     .build());
    }

    /**
     * Build a JPA Integraion Test Environment
     *
     * @param persistenceUnit name of persistence unit
     * @param databaseName    used by {@link PostgresITDataSource.Builder#fromProperty(java.lang.String)
     *                        }
     */
    public JPAIntegrationTestBase(String persistenceUnit, String databaseName) {
        this(persistenceUnit, "FINE", databaseName);
    }

    /**
     * Backup all tables and flush entity manager
     *
     * @throws SQLException if backup fails
     */
    @Before
    public void jpaSetup() throws SQLException {
        dataSource.copyAllTablesToDisk();
        jpaFlush();
    }

    /**
     * Truncate all tables and restore from backup
     *
     * @throws SQLException if restore fails
     */
    @After
    public void jpaRevert() throws SQLException {
        dataSource.truncateAllTables();
        dataSource.copyAllTablesFromDisk();
    }

    /**
     * Flush entity manager
     */
    public void jpaFlush() {
        entityManager.clear();
        entityManagerFactiory.getCache().evictAll();
    }

    /**
     * Run a code block with an entitymanager in a transaction
     *
     * @param code the code to run
     */
    public void jpa(VoidCode code) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            code.run(entityManager);
            transaction.commit();
        } catch (RuntimeException ex) {
            transaction.rollback();
            throw ex;
        } catch (Exception ex) {
            transaction.rollback();
            throw new RuntimeException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    public <T> T jpa(TCode<T> code) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            T result = code.run(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            transaction.rollback();
            throw ex;
        } catch (Exception ex) {
            transaction.rollback();
            throw new RuntimeException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    public interface VoidCode {

        /**
         * Code to run with a given EnitiyManager
         *
         * @param em entity manager
         * @throws Exception in case of any error
         */
        void run(EntityManager em) throws Exception;
    }

    @FunctionalInterface
    public interface TCode<T> {

        /**
         * Code to run with a given EnitiyManager
         *
         * @param em entity manager
         * @return any value
         * @throws Exception in case of any error
         */
        T run(EntityManager em) throws Exception;
    }
}

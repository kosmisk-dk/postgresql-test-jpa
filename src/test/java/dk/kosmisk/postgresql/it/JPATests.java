package dk.kosmisk.postgresql.it;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
public class JPATests extends JPAIntegrationTestBase {

    public JPATests(String persistenceUnit, String logLevel, PostgresITDataSource dataSource) {
        super(persistenceUnit, logLevel, dataSource);
    }

    @Test
    public void testJpaPersist() throws Exception {
        System.out.println("testJpaPersist");
        jpa(em -> {
            DataEntity dataEntity = new DataEntity(1, "abc");
            em.persist(dataEntity);
        });
        try (Connection connection = dataSource.getConnection() ;
             Statement stmt = connection.createStatement() ;
             ResultSet resultSet = stmt.executeQuery("SELECT key, data FROM data")) {
            while (resultSet.next()) {
                int key = resultSet.getInt(1);
                String data = resultSet.getString(2);
                System.out.println("key = " + key + "; data = " + data);
                if(key == 1 && data.equals("abc"))
                    return;
            }
        }
        fail("Could not persisted find data in database");
    }

    @Test
    public void testJpaPersistAlterRetrieve() throws Exception {
        System.out.println("testJpaPersistAlterRetrieve");
        jpa(em -> {
            DataEntity dataEntity = new DataEntity(1, "abc");
            em.persist(dataEntity);
        });
        try (Connection connection = dataSource.getConnection() ;
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.executeUpdate("UPDATE data SET data='def' WHERE key=1");
            connection.commit();
        }
        DataEntity entityBeforeFlush = jpa(em -> {
            return em.find(DataEntity.class, 1);
        });
        System.out.println("entity = " + entityBeforeFlush);
        assertNotNull(entityBeforeFlush);
        assertEquals(1, entityBeforeFlush.getKey());
        assertEquals("abc", entityBeforeFlush.getData());

        jpaFlush();

        DataEntity entityAfterFlush = jpa(em -> {
            return em.find(DataEntity.class, 1);
        });
        System.out.println("entity = " + entityAfterFlush);
        assertNotNull(entityAfterFlush);
        assertEquals(1, entityAfterFlush.getKey());
        assertEquals("def", entityAfterFlush.getData());
    }

    
}

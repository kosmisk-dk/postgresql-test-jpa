package dk.kosmisk.postgresql.it;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
public class JPAIntegrationTestBaseHibernateIT extends JPATests {

    public JPAIntegrationTestBaseHibernateIT() throws ClassNotFoundException {
        super("test_hibernate_PU", "FINE",
              PostgresITDataSource.builder()
                      .fromProperty("testbase")
                      .withFallback()
                      .build());
    }

}

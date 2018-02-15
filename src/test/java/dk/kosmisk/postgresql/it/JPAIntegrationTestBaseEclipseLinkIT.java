package dk.kosmisk.postgresql.it;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
public class JPAIntegrationTestBaseEclipseLinkIT extends JPATests {

    public JPAIntegrationTestBaseEclipseLinkIT() throws ClassNotFoundException {
        super("test_eclipselink_PU", "FINE",
              PostgresITDataSource.builder()
                      .fromProperty("testbase")
                      .withFallback()
                      .build());
    }

}

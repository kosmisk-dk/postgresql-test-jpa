# PostgreSQL Test JPA - An Integration Test Helper

## Integration testing of entities

For testing entities in a hibernate/eclipselink environment against a PostgreSQL
database.

The database could be set up by dk.kosmisk:postgresql-maven-plugin 
[https://github.com/kosmisk-dk/postgresql-maven-plugin](https://github.com/kosmisk-dk/postgresql-maven-plugin)



The project provides a JPAIntegrationTestBase class that on construction takes a
persistence unit, and a
[PostgreSQL DataSource](https://github.com/kosmisk-dk/postgresql-test-datasource).
Then it constructs an environment where tests can be performed.

The JPAIntegrationTestBase exposes 2 `jpa` methods one that takes a "Consumer"
and one that takes a "Function".

* Both methods are given an EntityManager in a transaction.
* Both can throw Exceptions.
* The Exceptions thrown are (if needed) wrapped in a RuntimeException.
* The "Function" variant of the `jpa` method returns the return value of the function.

It also exposes `jpaFlush` which flushes the EntityManager's cache

It implements both `@Before` and `@After` functions for dumping / restoring the
database, so that modifications to the database aren't seen by other tests.

Invocations could look like this:

        SomeType obj = jpa(em -> {
            return em.find(someKey, SomeType.class);
        });

or

        jpa(em -> {
            em.persist(someObject);
        });


A typical use case is outlined below:

### Typical Test Environment


        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>${some.version}</version>
            <configuration>
                <redirectTestOutputToFile>false</redirectTestOutputToFile>
                <systemPropertyVariables>
                    <postgresql.testbase.port>${postgresql.testbase.port}</postgresql.testbase.port>
                    <postgresql.dump.folder>${postgresql.dump.folder}</postgresql.dump.folder>
                </systemPropertyVariables>
                <argLine>-Dfile.encoding=${project.build.sourceEncoding}</argLine>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <plugin>
            <groupId>dk.kosmisk</groupId>
            <artifactId>postgresql-maven-plugin</artifactId>
            <version>${some.version}</version>
            <configuration>
                <!-- <version>LATEST</version> -->
            </configuration>
            <executions>
                <execution>
                    <id>postgresql-test-database</id>
                    <goals>
                        <goal>setup</goal>
                        <goal>startup</goal>
                        <goal>shutdown</goal>
                    </goals>
                    <configuration>
                        <name>testbase</name>
                    </configuration>
                </execution>
            </executions>
        </plugin>

...

        <dependency>
            <groupId>dk.kosmisk</groupId>
            <artifactId>postgresql-test-jpa</artifactId>
            <version>${some.version}</version>
            <type>jar</type>
        </dependency>


### Typical Test

        public class EntityTest extends dk.kosmisk.JPAIntegrationTestBase {

            public static final String PERSISTENCE_UNIT = "testPU";
            public static final String ECLIPSELINK_LOGLEVEL = "FINE";

            public EntityTest() {
                super(PERSISTENCE_UNIT, ECLIPSELINK_LOGLEVEL,
                      dk.kosmisk.PostgresITDataSource.builder()
                              .fromProperty("testbase")
                              .withFallback()
                              .build());
                DatabaseMigrator.migrate(datasource);
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
                fail("Could not find persisted data in database");
            }
        }

### META-INF/persistence.xml
#### Hibernate

        <persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_1.xsd"
                     version="2.1">
            <persistence-unit name="testPU" transaction-type="RESOURCE_LOCAL">
                <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
                <exclude-unlisted-classes>false</exclude-unlisted-classes>
                <properties>
                    <property name="javax.persistence.schema-generation.database.action" value="none"/>
                </properties>
            </persistence-unit>
        </persistence>

#### Eclipselink

        <persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_1.xsd"
                     version="2.1">
            <persistence-unit name="testPU" transaction-type="JTA">
                <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
                <jta-data-source>jdbc/foo</jta-data-source> 
                <exclude-unlisted-classes>false</exclude-unlisted-classes>
                <properties>
                    <property name="eclipselink.logging.logger" value="org.eclipse.persistence.logging.DefaultSessionLog"/>
                    <property name="eclipselink.logging.level" value="INFO"/>
                </properties>
            </persistence-unit>
        </persistence>

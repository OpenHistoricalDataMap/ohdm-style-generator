import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * You have to place files called geoserver-config.json and datasource-config.json with valid connection properties in src/test/resources/.env
 * in order for this test to work.
 */
public class IntegrationTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File ohdmConfigFile = Paths.get(resourceDirectory.toString(), "parser/example_complex.ohdmconfig").toFile();
    private File databaseConfigFile = Paths.get(resourceDirectory.toString(), ".env/datasource-config.json").toFile();
    private File connectionConfigFile = Paths.get(resourceDirectory.toString(), ".env/geoserver-config.json").toFile();
    private File outputDirectory = new File("out/slds");

    @Test
    void testWithDefaultsAndOutputDirectory() {
        String[] args = {"-d", "-o", outputDirectory.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        Main.main(args);
    }

    @Test
    void testWithDatabaseConfig() {
        String[] args = {"-db", databaseConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        Main.main(args);
    }

    @Test
    void testWithConnectionConfig() {
        String[] args = {"-c", connectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        Main.main(args);
    }

    @Test
    void testWithOutputDirectoryAndDBConfigAndConnectionConfig() {
        String[] args = {"-o", outputDirectory.getAbsolutePath(), "-db", databaseConfigFile.getAbsolutePath(), "-c", connectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        Main.main(args);
    }

    @Test
    void testWithDefaultsAndOutputDirectoryAndDBConfigAndConnectionConfig() {
        String[] args = {"-d", "-o", outputDirectory.getAbsolutePath(), "-db", databaseConfigFile.getAbsolutePath(), "-c", connectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        Main.main(args);
    }
}
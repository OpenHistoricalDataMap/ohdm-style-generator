package cli;

import logging.LoggerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * You have to place files called geoserver-config.json and datasource-config.json with valid connection properties in src/test/resources/.env
 * in order for this test to work.
 */
class GeneratorCommandTest {

    private CommandLine commandLine;
    private GeneratorCommand generatorCommand;

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File ohdmConfigFile = Paths.get(resourceDirectory.toString(), "parser/example_complex.ohdmconfig").toFile();
    private File databaseConfigFile = Paths.get(resourceDirectory.toString(), ".env/datasource-config.json").toFile();
    private File connectionConfigFile = Paths.get(resourceDirectory.toString(), ".env/geoserver-config.json").toFile();
    private File outputDirectory = new File("out/slds");

    @BeforeEach
    void setUp() {
        LoggerConfiguration.muteGeoServerLogs();
        generatorCommand = new GeneratorCommand();
        commandLine = new CommandLine(generatorCommand);
    }

    @Test
    void testCallWithoutOptionalArguments() {
        String[] args = {ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(GeneratorCommand.DEFAULT_OUTPUT_DIRECTORY.getAbsolutePath(), generatorCommand.outputDirectory.getAbsolutePath());
        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertNull(generatorCommand.databaseConfigFile);
        assertNull(generatorCommand.connectionConfigFile);

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }

    @Test
    void testCallWithOutputDirectory() {
        String[] args = {"-o", outputDirectory.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(outputDirectory.getAbsolutePath(), generatorCommand.outputDirectory.getAbsolutePath());
        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertNull(generatorCommand.databaseConfigFile);
        assertNull(generatorCommand.connectionConfigFile);

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }

    @Test
    void testCallWithDefaultsAndOutputDirectory() {
        String[] args = {"-d", "-o", outputDirectory.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(outputDirectory.getAbsolutePath(), generatorCommand.outputDirectory.getAbsolutePath());
        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertNull(generatorCommand.databaseConfigFile);
        assertNull(generatorCommand.connectionConfigFile);

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }

    @Test
    void testCallWithDatabaseConfig() {
        String[] args = {"-db", databaseConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertEquals(databaseConfigFile.getAbsolutePath(), generatorCommand.databaseConfigFile.getAbsolutePath());
        assertNull(generatorCommand.connectionConfigFile);

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }

    @Test
    void testCallWithNonExistentDatabaseConfig() {
        File invalidDatabaseConfigFile = new File("asdf");
        String[] args = {"-db", invalidDatabaseConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        int exitCode = commandLine.execute(args);

        assertTrue(exitCode != 0);

        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertEquals(invalidDatabaseConfigFile.getAbsolutePath(), generatorCommand.databaseConfigFile.getAbsolutePath());
        assertNull(generatorCommand.connectionConfigFile);
    }

    @Test
    void testCallWithConnectionConfig() {
        String[] args = {"-c", connectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertEquals(connectionConfigFile.getAbsolutePath(), generatorCommand.connectionConfigFile.getAbsolutePath());
        assertNull(generatorCommand.databaseConfigFile);

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }

    @Test
    void testCallWithNonExistentConnectionConfig() {
        File invalidConnectionConfigFile = new File("asdf");
        String[] args = {"-c", invalidConnectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        int exitCode = commandLine.execute(args);

        assertTrue(exitCode != 0);
        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());
        assertEquals(invalidConnectionConfigFile.getAbsolutePath(), generatorCommand.connectionConfigFile.getAbsolutePath());
        assertNull(generatorCommand.databaseConfigFile);
    }

    @Test
    void testCallWithOutputDirectoryAndDBConfigAndConnectionConfig() {
        String[] args = {"-o", outputDirectory.getAbsolutePath(), "-db", databaseConfigFile.getAbsolutePath(), "-c", connectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(outputDirectory.getAbsolutePath(), generatorCommand.outputDirectory.getAbsolutePath());
        assertEquals(databaseConfigFile.getAbsolutePath(), generatorCommand.databaseConfigFile.getAbsolutePath());
        assertEquals(connectionConfigFile.getAbsolutePath(), generatorCommand.connectionConfigFile.getAbsolutePath());
        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }

    @Test
    void testCallWithDefaultsAndOutputDirectoryAndDBConfigAndConnectionConfig() {
        String[] args = {"-d", "-o", outputDirectory.getAbsolutePath(), "-db", databaseConfigFile.getAbsolutePath(), "-c", connectionConfigFile.getAbsolutePath(), ohdmConfigFile.getAbsolutePath()};
        commandLine.execute(args);

        assertEquals(outputDirectory.getAbsolutePath(), generatorCommand.outputDirectory.getAbsolutePath());
        assertEquals(databaseConfigFile.getAbsolutePath(), generatorCommand.databaseConfigFile.getAbsolutePath());
        assertEquals(connectionConfigFile.getAbsolutePath(), generatorCommand.connectionConfigFile.getAbsolutePath());
        assertEquals(ohdmConfigFile.getAbsolutePath(), generatorCommand.ohdmConfigFile.getAbsolutePath());

        assertTrue(Objects.requireNonNull(generatorCommand.outputDirectory.listFiles()).length > 0);
    }
}
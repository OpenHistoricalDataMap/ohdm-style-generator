package geoserver;

import db.DataSourceConfig;
import db.DataSourceFactory;
import geoserver.connection.ServerConnectionConfig;
import geoserver.connection.ServerConnectionFactory;
import model.classification.OSMClassification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutils.TestUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * You have to place files called geoserver-config.json and datasource-config.json with valid connection properties in src/test/resources/.env
 * in order for this test to work.
 */
class GeoServerConfiguratorTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File validConnectionConfigFile = Paths.get(resourceDirectory.toString(), ".env/geoserver-config.json").toFile();
    private File validDatasourceConfigFile = Paths.get(resourceDirectory.toString(), ".env/datasource-config.json").toFile();

    private File tempDirectoryFile;

    @BeforeEach
    void setUp() throws IOException {
        Path tempDirectoryPath = Files.createTempDirectory("geoserver-test");
        tempDirectoryFile = tempDirectoryPath.toFile();
        TestUtils.generateSampleSLDsIntoDirectory(tempDirectoryFile);
    }

    @Test
    void testUploadSLDsToGeoServerWithValidConfig() throws FileNotFoundException {
        ServerConnectionConfig connectionConfig = ServerConnectionFactory.createConnectionConfigFromJSON(validConnectionConfigFile);
        GeoServerConfigurator geoServerConfigurator = GeoServerConfigurator.getInstance(connectionConfig);

        List<File> sldFiles = Arrays.asList(tempDirectoryFile.listFiles());
        assertDoesNotThrow(() -> {
            geoServerConfigurator.uploadSLDsToGeoServer(sldFiles);
        });
    }

    @Test
    void testUploadSLDsToGeoServerWithInvalidConfig() throws FileNotFoundException {
        ServerConnectionConfig connectionConfig = new ServerConnectionConfig();
        GeoServerConfigurator geoServerConfigurator = GeoServerConfigurator.getInstance(connectionConfig);

        List<File> sldFiles = Arrays.asList(tempDirectoryFile.listFiles());
        Assertions.assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.uploadSLDsToGeoServer(sldFiles);
        });

        connectionConfig.setPath("myPath");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.uploadSLDsToGeoServer(sldFiles);
        });
        connectionConfig.setHost("myHost");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.uploadSLDsToGeoServer(sldFiles);
        });
        connectionConfig.setUser("myUser");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.uploadSLDsToGeoServer(sldFiles);
        });
        connectionConfig.setPassword("myPassword");
        assertThrows(IllegalArgumentException.class, () -> {
            geoServerConfigurator.uploadSLDsToGeoServer(sldFiles);
        });
    }

    @Test
    void testConfigureGeoServerWithValidConfig() throws IOException {
        ServerConnectionConfig connectionConfig = ServerConnectionFactory.createConnectionConfigFromJSON(validConnectionConfigFile);
        GeoServerConfigurator geoServerConfigurator = GeoServerConfigurator.getInstance(connectionConfig);

        List<File> sldFiles = Arrays.asList(tempDirectoryFile.listFiles());
        assertDoesNotThrow(() -> {
            geoServerConfigurator.configureGeoServer(sldFiles);
        });
    }


    @Test
    void testConfigureGeoServerWithInvalidConfig() throws FileNotFoundException {
        ServerConnectionConfig connectionConfig = new ServerConnectionConfig();
        GeoServerConfigurator geoServerConfigurator = GeoServerConfigurator.getInstance(connectionConfig);

        List<File> sldFiles = Arrays.asList(tempDirectoryFile.listFiles());
        Assertions.assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServer(sldFiles);
        });

        connectionConfig.setPath("myPath");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServer(sldFiles);
        });
        connectionConfig.setHost("myHost");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServer(sldFiles);
        });
        connectionConfig.setUser("myUser");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServer(sldFiles);
        });
        connectionConfig.setPassword("myPassword");
        assertThrows(IllegalArgumentException.class, () -> {
            geoServerConfigurator.configureGeoServer(sldFiles);
        });
    }

    @Test
    void testConfigureGeoServerWithDataSourceWithValidConfigs() throws IOException {
        ServerConnectionConfig connectionConfig = ServerConnectionFactory.createConnectionConfigFromJSON(validConnectionConfigFile);
        GeoServerConfigurator geoServerConfigurator = GeoServerConfigurator.getInstance(connectionConfig);

        DataSourceConfig dataSourceConfig = DataSourceFactory.createDataSourceConfigFromJSON(validDatasourceConfigFile);
        OSMClassification classification = OSMClassification.getOSMClassification();
        List<File> sldFiles = Arrays.asList(tempDirectoryFile.listFiles());
        assertDoesNotThrow(() -> {
            geoServerConfigurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
        });
    }


    @Test
    void testConfigureGeoServerWithDataSourceWithInvalidConnectionConfig() throws FileNotFoundException {
        ServerConnectionConfig connectionConfig = new ServerConnectionConfig();
        GeoServerConfigurator geoServerConfigurator = GeoServerConfigurator.getInstance(connectionConfig);

        OSMClassification classification = OSMClassification.getOSMClassification();
        DataSourceConfig dataSourceConfig = DataSourceFactory.createDataSourceConfigFromJSON(validDatasourceConfigFile);

        List<File> sldFiles = Arrays.asList(tempDirectoryFile.listFiles());
        Assertions.assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
        });

        connectionConfig.setPath("myPath");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
        });
        connectionConfig.setHost("myHost");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
        });
        connectionConfig.setUser("myUser");
        assertThrows(NullPointerException.class, () -> {
            geoServerConfigurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
        });
        connectionConfig.setPassword("myPassword");
        assertThrows(IllegalArgumentException.class, () -> {
            geoServerConfigurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
        });

    }


}
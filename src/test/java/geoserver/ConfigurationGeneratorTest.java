package geoserver;

import db.DataSourceConfig;
import geoserver.connection.ServerConnectionConfig;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationGeneratorTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File sampleConfig = Paths.get(resourceDirectory.toString(), "parser/example_simple.ohdmconfig").toFile();

    private File tempDirectoryFile;

    @BeforeEach
    void setUp() throws IOException {
        Path tempDirectoryPath = Files.createTempDirectory("geoserver-test");
        tempDirectoryFile = tempDirectoryPath.toFile();

        TestUtils.generateSampleSLDsIntoDirectory(tempDirectoryFile);
    }

    @Test
    void testCreateStyleInfoFilesWithEmptyConfig() throws IOException {
        List<File> sldFileList = Arrays.asList(tempDirectoryFile.listFiles());
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFileList);


        ServerConnectionConfig serverConnectionConfig = new ServerConnectionConfig();


        assertDoesNotThrow(() -> {
                    configurationGenerator.createStyleInfoFiles(tempDirectoryFile, serverConnectionConfig);
                }
        );

        // assert the StyleInfo files are in the "styles" subdirectory because since the config is empty they should be
        // treated as global styles
        File globalStylesDirectory = Paths.get(tempDirectoryFile.getAbsolutePath(), "styles").toFile();
        List<File> styleInfoFileList = Arrays.asList(globalStylesDirectory.listFiles());

        assertEquals(sldFileList.size(), styleInfoFileList.size());

        // assert that every SLD file has their counterpart
        for (File sldFile : sldFileList) {
            File matchingFile = styleInfoFileList.stream().filter(file -> file.getName().contains(FilenameUtils.removeExtension(sldFile.getName()))).findAny().get();
            assertNotNull(matchingFile);
        }
    }

    @Test
    void testCreateStyleInfoFilesWithWorkspaceSpecificConfig() throws IOException {
        List<File> sldFileList = Arrays.asList(tempDirectoryFile.listFiles());
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFileList);

        ServerConnectionConfig serverConnectionConfig = new ServerConnectionConfig();
        serverConnectionConfig.setWorkspaceName("myWorkspaceName");
        serverConnectionConfig.setNamespaceName("myNamespaceName");

        assertDoesNotThrow(() -> {
                    configurationGenerator.createStyleInfoFiles(tempDirectoryFile, serverConnectionConfig);
                }
        );

        // assert the StyleInfo files are in the workspace specific subdirectory because workspace and namespace are defined in config
        File workspaceStyleDirectory = Paths.get(tempDirectoryFile.getAbsolutePath(), "workspaces", serverConnectionConfig.getWorkspaceName(), "styles").toFile();
        List<File> styleInfoFileList = Arrays.asList(workspaceStyleDirectory.listFiles());

        assertEquals(sldFileList.size(), styleInfoFileList.size());

        // assert that every SLD file has their counterpart
        for (File sldFile : sldFileList) {
            File matchingFile = styleInfoFileList.stream().filter(file -> file.getName().contains(FilenameUtils.removeExtension(sldFile.getName()))).findAny().get();
            assertNotNull(matchingFile);
        }
    }

    @Test
    void testCreateStyleInfoFilesWithMissingNamespaceName() throws IOException {
        List<File> sldFileList = Arrays.asList(tempDirectoryFile.listFiles());
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFileList);

        ServerConnectionConfig serverConnectionConfig = new ServerConnectionConfig();
        serverConnectionConfig.setWorkspaceName("myWorkspaceName");

        assertDoesNotThrow(() -> {
                    configurationGenerator.createStyleInfoFiles(tempDirectoryFile, serverConnectionConfig);
                }
        );

        // assert the StyleInfo files are in the global styles directory since the namespace name is missing
        File globalStylesDirectory = Paths.get(tempDirectoryFile.getAbsolutePath(), "styles").toFile();
        List<File> styleInfoFileList = Arrays.asList(globalStylesDirectory.listFiles());

        assertEquals(sldFileList.size(), styleInfoFileList.size());

        // assert that every SLD file has their counterpart
        for (File sldFile : sldFileList) {
            File matchingFile = styleInfoFileList.stream().filter(file -> file.getName().contains(FilenameUtils.removeExtension(sldFile.getName()))).findAny().get();
            assertNotNull(matchingFile);
        }
    }

    @Test
    void testCreateConfigurationFilesWithInvalidServerConnectionConfigs() {
        List<File> sldFileList = Arrays.asList(tempDirectoryFile.listFiles());
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFileList);

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setHost("myHost");
        dataSourceConfig.setDatabase("myDatabase");
        dataSourceConfig.setUser("myUser");
        dataSourceConfig.setPassword("myPassword");
        dataSourceConfig.setPort(5432);
        ServerConnectionConfig serverConnectionConfig = new ServerConnectionConfig();

        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        serverConnectionConfig.setWorkspaceName("myWorkspaceName");

        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        serverConnectionConfig.setNamespaceName("myNamespaceName");

        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        serverConnectionConfig.setStoreName("myStoreName");

        assertDoesNotThrow(() -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });
    }

    @Test
    void testCreateConfigurationFilesWithInvalidDataSourceConfigs() {
        List<File> sldFileList = Arrays.asList(tempDirectoryFile.listFiles());
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFileList);

        ServerConnectionConfig serverConnectionConfig = new ServerConnectionConfig();
        serverConnectionConfig.setWorkspaceName("myWorkspaceName");
        serverConnectionConfig.setNamespaceName("myNamespaceName");
        serverConnectionConfig.setStoreName("myStoreName");
        DataSourceConfig dataSourceConfig = new DataSourceConfig();

        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        dataSourceConfig.setHost("myHost");
        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        dataSourceConfig.setDatabase("myDatabase");
        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        dataSourceConfig.setUser("myUser");
        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });


        dataSourceConfig.setPassword("myPassword");
        assertThrows(NullPointerException.class, () -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        dataSourceConfig.setPort(5432);
        assertDoesNotThrow(() -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });
    }

    @Test
    void testCreateConfigurationFilesWithValidConfigs() {
        List<File> sldFileList = Arrays.asList(tempDirectoryFile.listFiles());
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFileList);

        ServerConnectionConfig serverConnectionConfig = new ServerConnectionConfig();
        serverConnectionConfig.setWorkspaceName("myWorkspaceName");
        serverConnectionConfig.setNamespaceName("myNamespaceName");
        serverConnectionConfig.setStoreName("myStoreName");
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setHost("myHost");
        dataSourceConfig.setDatabase("myDatabase");
        dataSourceConfig.setUser("myUser");
        dataSourceConfig.setPassword("myPassword");
        dataSourceConfig.setPort(5432);

        assertDoesNotThrow(() -> {
            configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryFile);
        });

        // check workspace directory
        File myWorkspaceFile = Paths.get(tempDirectoryFile.getAbsolutePath(), "workspaces", serverConnectionConfig.getWorkspaceName()).toFile();
        List<String> workspaceFiles = Arrays.asList(myWorkspaceFile.list());
        assertTrue(workspaceFiles.contains("namespace.xml"));
        assertTrue(workspaceFiles.contains("workspace.xml"));
        assertTrue(workspaceFiles.contains("myStoreName"));
        assertTrue(workspaceFiles.contains("styles"));

        // check store directory
        File myStoreFile = Paths.get(tempDirectoryFile.getAbsolutePath(), "workspaces", serverConnectionConfig.getWorkspaceName(), serverConnectionConfig.getStoreName()).toFile();
        List<String> storeFileList = Arrays.asList(myStoreFile.list());
        assertTrue(storeFileList.contains("datastore.xml"));
        // assert that every SLD file has a directory in the store directory
        for (File sldFile : sldFileList) {
            String matchingDirectory = storeFileList.stream().filter(s -> s.equals(FilenameUtils.removeExtension(sldFile.getName()))).findAny().get();
            assertNotNull(matchingDirectory);

            // assert that every directory contains files for FeatureTypeInfo and LayerInfo
            File storeDirectoryForSLD = Paths.get(myStoreFile.getAbsolutePath(), matchingDirectory).toFile();
            List<String> fileNames = Arrays.asList(storeDirectoryForSLD.list());
            assertTrue(fileNames.contains("featuretype.xml"));
            assertTrue(fileNames.contains("layer.xml"));
        }

        // check styles directory
        File myStylesFile = Paths.get(tempDirectoryFile.getAbsolutePath(), "workspaces", serverConnectionConfig.getWorkspaceName(), "styles").toFile();
        List<File> styleInfoFiles = Arrays.asList(myStylesFile.listFiles());
        for (File sldFile : sldFileList) {
            File matchingFile = styleInfoFiles.stream().filter(file -> file.getName().contains(FilenameUtils.removeExtension(sldFile.getName()))).findAny().get();
            assertNotNull(matchingFile);
        }
    }
}
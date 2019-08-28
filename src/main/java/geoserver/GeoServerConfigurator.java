package geoserver;

import db.DataSourceConfig;
import db.DataSourceWrapper;
import db.DatabaseConfigurator;
import geoserver.connection.ServerConnection;
import geoserver.connection.ServerConnectionConfig;
import model.classification.Classification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

public class GeoServerConfigurator {

    private static GeoServerConfigurator instance;

    private static final String RELATIVE_PATH_TO_STYLE_DIRECTORY = "/styles/";

    private final Logger logger = LogManager.getLogger(GeoServerConfigurator.class);
    private ServerConnectionConfig serverConnectionConfig;
    private ServerConnection serverConnection;


    public static synchronized GeoServerConfigurator getInstance(ServerConnectionConfig serverConnectionConfig) {
        if (instance == null || serverConnectionConfig != instance.serverConnectionConfig) {
            instance = new GeoServerConfigurator(serverConnectionConfig);
        }
        return instance;
    }

    private GeoServerConfigurator(ServerConnectionConfig serverConnectionConfig) {
        this.serverConnectionConfig = serverConnectionConfig;
        serverConnection = new ServerConnection();
    }

    /**
     * Uploads the supplied sldFiles to the server.
     *
     * @param sldFiles The sldFiles which should be uploaded
     * @throws NullPointerException     if the connection config is missing obligatory parameters
     * @throws IllegalArgumentException if the connection to the server cannot be established
     */
    public void uploadSLDsToGeoServer(Collection<File> sldFiles) throws NullPointerException, IllegalArgumentException {
        assertServerConnectionConfig(serverConnectionConfig);

        boolean connected = serverConnection.connect(serverConnectionConfig);
        if (!connected) {
            throw new IllegalArgumentException("Could not connect to server. Aborting upload of SLDs.");
        }

        String geoServerRootPath = serverConnectionConfig.getPath();
        Path destinationPath;
        if (serverConnectionConfig.getWorkspaceName() != null) {
            // this is the path to the workspace specific styles directory
            destinationPath = Paths.get(geoServerRootPath, "workspaces", serverConnectionConfig.getWorkspaceName(), RELATIVE_PATH_TO_STYLE_DIRECTORY);
        } else {
            // this is the path to the global styles directory
            destinationPath = Paths.get(geoServerRootPath, RELATIVE_PATH_TO_STYLE_DIRECTORY);
        }

        for (File sldFile : sldFiles) {
            Path destinationFilePath = Paths.get(destinationPath.toString(), sldFile.getName());
            serverConnection.uploadFileToServer(sldFile, destinationFilePath.toString());
        }

        serverConnection.disconnect();
    }

    /**
     * Creates and uploads StyleInfo files for the supplied sldFiles
     *
     * @param sldFiles The sldFiles for which StyleInfo files should be generated
     * @throws NullPointerException     if the connection config is missing obligatory parameters
     * @throws IllegalArgumentException if the connection to the server cannot be established
     */
    public void configureGeoServer(Collection<File> sldFiles) throws NullPointerException, IllegalArgumentException {
        assertServerConnectionConfig(serverConnectionConfig);

        boolean connected = serverConnection.connect(serverConnectionConfig);
        if (!connected) {
            throw new IllegalArgumentException("Could not connect to server. Aborting configuration of GeoServer.");
        }

        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFiles);
        try {
            Path tempDirectoryPath = Files.createTempDirectory("geoserver");
            Collection<File> styleInfoFiles = configurationGenerator.createStyleInfoFiles(tempDirectoryPath.toFile(), serverConnectionConfig);
            uploadConfigurationFiles(styleInfoFiles, tempDirectoryPath.toFile());
        } catch (IOException e) {
            logger.error("Something went wrong while creating a temporary directory. \n\t Cause: {}", e.getMessage());
        }

        serverConnection.disconnect();
    }

    /**
     * Creates and uploads all possible configuration files for the supplied sldFiles.
     * Additionally creates tables for the specified if they are missing based on the supplied classification.
     *
     * @param classification   The classification which should be used for the database configuration
     * @param sldFiles         The sldFiles for which the configuration files should be generated
     * @param dataSourceConfig The configuration that contains the connection parameters for database access
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public void configureGeoServerWithDataSource(@NotNull Classification classification, Collection<File> sldFiles, DataSourceConfig dataSourceConfig) throws NullPointerException, IllegalArgumentException {
        assertServerConnectionConfig(serverConnectionConfig);

        boolean connected = serverConnection.connect(serverConnectionConfig);
        if (!connected) {
            throw new IllegalArgumentException("Could not connect to server. Aborting configuration of GeoServer.");
        }

        try {
            Objects.requireNonNull(classification, "Classification cannot be null.");
            createDatabaseLayerTables(classification, dataSourceConfig);
            Path tempDirectoryPath = Files.createTempDirectory("geoserver");
            ConfigurationGenerator configurationGenerator = new ConfigurationGenerator(sldFiles);
            Collection<File> configurationFiles = configurationGenerator.createConfigurationFiles(dataSourceConfig, serverConnectionConfig, tempDirectoryPath.toFile());
            uploadConfigurationFiles(configurationFiles, tempDirectoryPath.toFile());
        } catch (SQLException e) {
            logger.error("Something went wrong while creating tables for the classification. \n\t Cause: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("Something went wrong while creating a temporary directory. \n\t Cause: {}", e.getMessage());
        }
    }

    private void uploadConfigurationFiles(Collection<File> configurationFiles, File directoryRoot) {
        for (File file : configurationFiles) {
            assert serverConnection.isConnected();

            String pathRelativeToRoot = getPathRelativeToRoot(file.getAbsolutePath(), directoryRoot.getAbsolutePath());
            String pathOnGeoServer = Paths.get(serverConnectionConfig.getPath(), pathRelativeToRoot).toString();
            serverConnection.uploadFileToServer(file, pathOnGeoServer);
        }
    }

    /**
     * Reduces the absolute path of the file by the path to the temporary directory so that they are placed in the correct
     * directory on the GeoServer.
     */
    private String getPathRelativeToRoot(String extendedPath, String rootPath) {
        return extendedPath.replace(rootPath, "").substring(1);
    }

    private void createDatabaseLayerTables(@NotNull Classification classification, DataSourceConfig dataSourceConfig) throws SQLException {
        DataSourceWrapper dataSourceWrapper = new DataSourceWrapper(dataSourceConfig);

        DatabaseConfigurator databaseConfigurator = new DatabaseConfigurator(dataSourceWrapper);
        databaseConfigurator.createTablesForClassification(classification);
    }

    private void assertServerConnectionConfig(ServerConnectionConfig serverConnectionConfig) throws NullPointerException {
        Objects.requireNonNull(this.serverConnectionConfig.getPath(), "The path to the GeoServer data directory cannot be null.");
        Objects.requireNonNull(this.serverConnectionConfig.getHost(), "The host address cannot be null.");
        Objects.requireNonNull(this.serverConnectionConfig.getUser(), "The supplied user cannot be null.");
        Objects.requireNonNull(this.serverConnectionConfig.getPassword(), "The supplied password cannot be null.");
    }
}

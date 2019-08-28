package geoserver;

import db.DataSourceConfig;
import geoserver.cataloginfo.CatalogInfoFactory;
import geoserver.cataloginfo.CatalogInfoPersister;
import geoserver.connection.ServerConnectionConfig;
import geoserver.connection.ServerConnectionFactory;
import logging.LoggerConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoserver.catalog.*;
import org.geoserver.catalog.impl.CatalogImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.impl.GeoServerImpl;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class ConfigurationGenerator {

    private final Logger logger = LogManager.getLogger(ConfigurationGenerator.class);

    @NotNull
    private Collection<File> sldSourceFiles;

    private GeoServer geoServer;
    private CatalogInfoFactory catalogInfoFactory;

    public ConfigurationGenerator(@NotNull Collection<File> sldSourceFiles) {
        this.sldSourceFiles = sldSourceFiles;

        LoggerConfiguration.muteGeoServerLogs();

        geoServer = new GeoServerImpl();
        geoServer.setCatalog(new CatalogImpl());

        catalogInfoFactory = new CatalogInfoFactory(geoServer.getCatalog());
    }

    /**
     * Creates a StyleInfo object for every sldSourceFile and saves it to a subdirectory of the targetDirectory.
     *
     * @param targetDirectory        The root of the directory where the StyleInfo files are saved.
     * @param serverConnectionConfig The connection config that contains additional information.
     * @return A collection of files for the StyleInfo objects.
     */
    public Collection<File> createStyleInfoFiles(@NotNull File targetDirectory, @NotNull ServerConnectionConfig serverConnectionConfig) {
        CatalogInfoPersister catalogInfoPersister = getPersister(targetDirectory);
        Collection<File> files = new ArrayList<>();

        WorkspaceInfo workspace = null;
        if (serverConnectionConfig.getWorkspaceName() != null) {
            if (serverConnectionConfig.getNamespaceName() != null) {
                workspace = getWorkspaceFromName(serverConnectionConfig.getWorkspaceName());
                NamespaceInfo namespace = catalogInfoFactory.createNamespaceInfo(workspace.getName(), serverConnectionConfig.getNamespaceName());
                files.add(catalogInfoPersister.saveWorkspaceInfo(workspace));
                files.add(catalogInfoPersister.saveNamespaceInfo(namespace));
            } else {
                logger.warn("You need to specify both, a workspace and namespace name if you want to create StyleInfo files for a specific workspace.");
            }
        }

        if (workspace != null) {
            logger.info("Creating StyleInfo files for workspace {}.", workspace);
        } else {
            logger.info("Creating StyleInfo files without specific workspace.");
        }

        for (File sldSourceFile : sldSourceFiles) {
            String nameWithoutExtension = FilenameUtils.removeExtension(sldSourceFile.getName());
            StyleInfo styleInfo = catalogInfoFactory.createStyleInfo(nameWithoutExtension, nameWithoutExtension + ".sld", workspace);
            File styleInfoFile = catalogInfoPersister.saveStyleInfo(styleInfo);
            files.add(styleInfoFile);
        }
        return files;
    }

    private WorkspaceInfo getWorkspaceFromName(String workspaceName) {
        WorkspaceInfo workspace = geoServer.getCatalog().getWorkspace(workspaceName);
        if (workspace == null) {
            workspace = catalogInfoFactory.createWorkspaceInfo(workspaceName);
        }
        return workspace;
    }

    /**
     * Creates the configuration files for WorkspaceInfo, NamespaceInfo, StoreInfo, StyleInfo, FeatureTypeInfo and LayerInfo
     * based on parameters in the DataSourceConfig and saves them to the targetDirectory.
     *
     * @param dataSourceConfig The DataSourceConfig that contains information on names used by configuration.
     * @param targetDirectory  The directory where the configuration files are saved.
     * @return A collection of files for the generated objects.
     */
    public Collection<File> createConfigurationFiles(@NotNull DataSourceConfig dataSourceConfig, @NotNull ServerConnectionConfig serverConnectionConfig, @NotNull File targetDirectory) {
        assertServerConnectionParameters(serverConnectionConfig);
        assertDataSourceParameters(dataSourceConfig);

        Catalog catalog = geoServer.getCatalog();
        CatalogInfoPersister catalogInfoPersister = getPersister(targetDirectory);
        Collection<File> files = new ArrayList<>();

        String workspaceName = serverConnectionConfig.getWorkspaceName();
        String namespaceName = serverConnectionConfig.getNamespaceName();
        String dataStoreName = serverConnectionConfig.getStoreName();
        String crsCode = serverConnectionConfig.getCrsCode();

        NamespaceInfo namespaceInfo = catalogInfoFactory.createNamespaceInfo(workspaceName, namespaceName); // namespace prefix usually refers to workspace name
        WorkspaceInfo workspaceInfo = catalogInfoFactory.createWorkspaceInfo(workspaceName);

        StoreInfo dataStoreInfo = catalogInfoFactory.createDataStoreInfo(workspaceInfo, dataStoreName, "PostGIS");
        Map<String, Serializable> parameters = ServerConnectionFactory.getConnectionParametersFrom(dataSourceConfig, namespaceInfo);
        dataStoreInfo.getConnectionParameters().putAll(parameters);
        catalog.save(dataStoreInfo); // save so that the catalog can use it to retrieve bounds of feature types etc.

        File namespaceInfoFile = catalogInfoPersister.saveNamespaceInfo(namespaceInfo);
        files.add(namespaceInfoFile);
        File workspaceInfoFile = catalogInfoPersister.saveWorkspaceInfo(workspaceInfo);
        files.add(workspaceInfoFile);
        File storeInfoFile = catalogInfoPersister.saveStoreInfo(dataStoreInfo);
        files.add(storeInfoFile);

        for (File sldSourceFile : sldSourceFiles) {
            String nameWithoutExtension = FilenameUtils.removeExtension(sldSourceFile.getName());

            StyleInfo styleInfo = getStyleInfoForName(nameWithoutExtension, workspaceInfo);
            File styleInfoFile = catalogInfoPersister.saveStyleInfo(styleInfo);
            files.add(styleInfoFile);

            FeatureTypeInfo featureTypeInfo = createFeatureTypeInfoForClassName(nameWithoutExtension, dataStoreInfo, namespaceInfo, crsCode);
            File featureTypeInfoFile = catalogInfoPersister.saveFeatureTypeInfo(featureTypeInfo);
            files.add(featureTypeInfoFile);

            LayerInfo layerInfoForFeatureType = createLayerInfoForFeatureType(featureTypeInfo, styleInfo);
            File layerInfoFile = catalogInfoPersister.saveLayerInfo(layerInfoForFeatureType);
            files.add(layerInfoFile);
        }

        return files;
    }

    private void assertServerConnectionParameters(ServerConnectionConfig serverConnectionConfig) {
        Objects.requireNonNull(serverConnectionConfig.getWorkspaceName(), "A workspace name is required to generate the configuration files.");
        Objects.requireNonNull(serverConnectionConfig.getNamespaceName(), "A namespace name is required to generate the configuration files.");
        Objects.requireNonNull(serverConnectionConfig.getStoreName(), "A data-store name is required to generate the configuration files.");

    }

    private void assertDataSourceParameters(DataSourceConfig dataSourceConfig) {
        Objects.requireNonNull(dataSourceConfig.getHost(), "A host address is required to access the data source.");
        if (dataSourceConfig.getPort() == 0) {
            throw new NullPointerException("A valid port number is required to access the data source.");
        }
        Objects.requireNonNull(dataSourceConfig.getDatabase(), "A database name is required to access the data source.");
        Objects.requireNonNull(dataSourceConfig.getUser(), "A user name is required to access the data source.");
        Objects.requireNonNull(dataSourceConfig.getPassword(), "A password is required to access the data source.");
    }

    private StyleInfo getStyleInfoForName(String name, WorkspaceInfo workspaceInfo) {
        StyleInfo styleInfo = geoServer.getCatalog().getStyleByName(name);
        if (styleInfo == null) {
            styleInfo = createStyleInfo(name, workspaceInfo);
        }
        return styleInfo;
    }

    private FeatureTypeInfo createFeatureTypeInfoForClassName(String className, StoreInfo storeInfo, NamespaceInfo namespaceInfo, String crsCode) {
        return catalogInfoFactory.createFeatureTypeInfo(className, storeInfo, namespaceInfo, crsCode);
    }

    private LayerInfo createLayerInfoForFeatureType(FeatureTypeInfo featureTypeInfo, StyleInfo styleInfo) {
        return catalogInfoFactory.createLayerInfo(featureTypeInfo.getName(), styleInfo, featureTypeInfo);
    }

    private StyleInfo createStyleInfo(String name, WorkspaceInfo workspaceInfo) {
        return catalogInfoFactory.createStyleInfo(name, name + ".sld", workspaceInfo);
    }

    private CatalogInfoPersister getPersister(@NotNull File targetDirectory) {
        XStreamPersister xmlPersister = new XStreamPersisterFactory().createXMLPersister();
        xmlPersister.setGeoServer(geoServer);
        xmlPersister.setCatalog(geoServer.getCatalog());
        return new CatalogInfoPersister(xmlPersister, targetDirectory);
    }
}

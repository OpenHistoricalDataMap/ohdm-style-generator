package geoserver.cataloginfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoserver.catalog.*;
import org.geoserver.config.util.XStreamPersister;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CatalogInfoPersister {
    private final Logger logger = LogManager.getLogger(CatalogInfoPersister.class);

    private File dataDirectory;
    private XStreamPersister xmlPersister;

    public CatalogInfoPersister(XStreamPersister xmlPersister, File dataDirectory) {
        this.xmlPersister = xmlPersister;
        this.dataDirectory = dataDirectory;
    }

    public File saveNamespaceInfo(NamespaceInfo namespaceInfo) {
        File namespaceFile = Paths.get(getPathToWorkspaces().toString(), namespaceInfo.getPrefix(), "namespace.xml").toFile();
        saveObjectToFile(namespaceInfo, namespaceFile);
        return namespaceFile;
    }

    public File saveWorkspaceInfo(WorkspaceInfo workspaceInfo) {
        File workspaceFile = Paths.get(getPathToWorkspace(workspaceInfo).toString(), "workspace.xml").toFile();
        saveObjectToFile(workspaceInfo, workspaceFile);
        return workspaceFile;
    }

    public File saveStoreInfo(StoreInfo storeInfo) {
        File storeInfoFile =
                Paths.get(getPathToDataStore(storeInfo).toString(), "datastore.xml").toFile();
        saveObjectToFile(storeInfo, storeInfoFile);
        return storeInfoFile;
    }

    public File saveFeatureTypeInfo(FeatureTypeInfo featureTypeInfo) {
        File featureTypeInfoFile = Paths.get(getPathToDataStore(featureTypeInfo.getStore()).toString(), featureTypeInfo.getNativeName(), "featuretype.xml").toFile();
        saveObjectToFile(featureTypeInfo, featureTypeInfoFile);
        return featureTypeInfoFile;
    }

    public File saveLayerInfo(LayerInfo layerInfo) {
        File layerInfoFile = Paths.get(getPathToDataStore(layerInfo.getResource().getStore()).toString(), layerInfo.getResource().getNativeName(), "layer.xml").toFile();
        saveObjectToFile(layerInfo, layerInfoFile);
        return layerInfoFile;
    }

    public File saveStyleInfo(StyleInfo styleInfo) {
        File styleInfoFile = Paths.get(getPathToStylesDirectory(styleInfo).toString(), styleInfo.getName() + ".xml").toFile();
        saveObjectToFile(styleInfo, styleInfoFile);
        return styleInfoFile;
    }

    private Path getPathToWorkspaces() {
        return Paths.get(dataDirectory.getAbsolutePath(), "workspaces");
    }

    private Path getPathToWorkspace(WorkspaceInfo workspaceInfo) {
        return Paths.get(getPathToWorkspaces().toString(), workspaceInfo.getName());
    }

    private Path getPathToDataStore(StoreInfo storeInfo) {
        return Paths.get(getPathToWorkspace(storeInfo.getWorkspace()).toString(), storeInfo.getName());
    }

    private Path getPathToStylesDirectory(StyleInfo styleInfo) {
        if (styleInfo.getWorkspace() != null) {
            return Paths.get(dataDirectory.getAbsolutePath(), "workspaces", styleInfo.getWorkspace().getName(), "styles");
        } else {
            return Paths.get(dataDirectory.getAbsolutePath(), "styles");
        }
    }

    private boolean saveObjectToFile(Object object, File file) {
        try {
            Files.createDirectories(Paths.get(file.getParentFile().getAbsolutePath()));
            xmlPersister.save(object, new FileOutputStream(file));
            logger.info("Saved object {} to file {}", object, file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Something went wrong while trying to save object {} to file {}" + file, object, file, e);
            return false;
        }
    }
}

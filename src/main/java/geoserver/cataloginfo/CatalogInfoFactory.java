package geoserver.cataloginfo;

import geoserver.cataloginfo.wrapper.*;
import org.geoserver.catalog.*;
import org.geoserver.catalog.impl.*;
import org.jetbrains.annotations.Nullable;

public class CatalogInfoFactory {

    private final Catalog catalog;

    public CatalogInfoFactory(Catalog catalog) {
        this.catalog = catalog;
    }

    public WorkspaceInfo createWorkspaceInfo(String name) {
        WorkspaceInfoImpl workspaceInfoImpl = new WorkspaceInfoImpl();
        WorkspaceInfoWrapper workspaceInfoWrapper = new WorkspaceInfoWrapper(workspaceInfoImpl, name);
        return workspaceInfoWrapper.getWorkspace();
    }

    public NamespaceInfo createNamespaceInfo(String prefix, String uri) {
        NamespaceInfoImpl namespaceInfoImpl = new NamespaceInfoImpl();
        NamespaceInfoWrapper namespaceInfoWrapper = new NamespaceInfoWrapper(namespaceInfoImpl, prefix, uri);
        return namespaceInfoWrapper.getNamespace();
    }

    public StoreInfo createDataStoreInfo(WorkspaceInfo workspaceInfo, String name, String type) {
        DataStoreInfo dataStoreInfo = catalog.getFactory().createDataStore();
        DataStoreInfoWrapper dataStoreInfoWrapper = new DataStoreInfoWrapper(dataStoreInfo, workspaceInfo, name, type);
        return dataStoreInfoWrapper.getDataStore();
    }

    public FeatureTypeInfo createFeatureTypeInfo(String nativeName, StoreInfo storeInfo, NamespaceInfo namespaceInfo, String crsCode) {
        FeatureTypeInfoImpl featureTypeInfoImpl = new FeatureTypeInfoImpl(catalog);
        featureTypeInfoImpl.setSRS(crsCode);
        CatalogBuilder catalogBuilder = new CatalogBuilder(catalog);
        FeatureTypeInfoWrapper featureTypeInfoWrapper = new FeatureTypeInfoWrapper(featureTypeInfoImpl, nativeName, storeInfo, namespaceInfo, catalogBuilder);
        return featureTypeInfoWrapper.getFeatureTypeInfo();
    }

    public LayerInfo createLayerInfo(String name, StyleInfo styleInfo, ResourceInfo resourceInfo) {
        LayerInfoImpl layerInfo = new LayerInfoImpl();
        LayerInfoWrapper layerInfoWrapper = new LayerInfoWrapper(layerInfo, name, styleInfo, resourceInfo);
        return layerInfoWrapper.getLayerInfo();
    }

    public StyleInfo createStyleInfo(String name, String filename, @Nullable WorkspaceInfo workspaceInfo) {
        StyleInfoImpl styleInfoImpl = new StyleInfoImpl(catalog);
        if (workspaceInfo != null)
            styleInfoImpl.setWorkspace(workspaceInfo);
        StyleInfoWrapper styleInfoWrapper = new StyleInfoWrapper(styleInfoImpl, name, filename);
        return styleInfoWrapper.getStyleInfo();
    }

}

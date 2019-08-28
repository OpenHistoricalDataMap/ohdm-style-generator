package geoserver.cataloginfo.wrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoserver.catalog.*;
import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import java.util.UUID;

public class FeatureTypeInfoWrapper {

    private static final String DEFAULT_CRS = "EPSG:3857";

    private final Logger logger = LogManager.getLogger(FeatureTypeInfoWrapper.class);

    private FeatureTypeInfo featureTypeInfo;

    public FeatureTypeInfoWrapper(FeatureTypeInfoImpl featureTypeInfoImpl, String nativeName, StoreInfo dataStore, NamespaceInfo namespace, CatalogBuilder catalogBuilder) {
        featureTypeInfoImpl.setId(generateID());
        featureTypeInfoImpl.setStore(dataStore);
        featureTypeInfoImpl.setNamespace(namespace);
        featureTypeInfoImpl.setEnabled(true);
        featureTypeInfoImpl.setNativeName(nativeName); //must match the table name
        featureTypeInfoImpl.setName(nativeName);
        if (featureTypeInfoImpl.getSRS() == null)
            featureTypeInfoImpl.setSRS(DEFAULT_CRS);
        featureTypeInfoImpl.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        featureTypeInfo = featureTypeInfoImpl;

        trySetNativeCRS(DEFAULT_CRS);
        trySetBounds(catalogBuilder);
    }

    private static String generateID() {
        return "FeatureTypeInfoImpl--" + UUID.randomUUID().toString();
    }

    private void trySetBounds(CatalogBuilder catalogBuilder) {
        try {
            ReferencedEnvelope nativeBounds = catalogBuilder.getNativeBounds(featureTypeInfo);
            ReferencedEnvelope latLonBounds = catalogBuilder.getLatLonBounds(nativeBounds, featureTypeInfo.getNativeCRS());
            featureTypeInfo.setNativeBoundingBox(nativeBounds);
            featureTypeInfo.setLatLonBoundingBox(latLonBounds);
        } catch (Exception e) {
            logger.error("Could not retrieve bounds of {}. Please check your database connection parameters. \n\t Cause: {}", featureTypeInfo.getName(), e.getCause());
        }
    }

    private void trySetNativeCRS(String crsCode) {
        try {
            featureTypeInfo.setNativeCRS(CRS.decode(crsCode));
        } catch (Exception e) {
            logger.error("Could not set native crs of {}. The decoding of crs code {} failed. \n\t Cause: {}", featureTypeInfo.getName(), crsCode, e.getCause());
        }
    }

    public FeatureTypeInfo getFeatureTypeInfo() {
        return featureTypeInfo;
    }
}

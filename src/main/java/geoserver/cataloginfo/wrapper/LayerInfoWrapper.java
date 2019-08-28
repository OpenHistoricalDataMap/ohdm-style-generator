package geoserver.cataloginfo.wrapper;

import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.impl.LayerInfoImpl;

import java.util.UUID;

public class LayerInfoWrapper {

    private LayerInfo layerInfo;

    public LayerInfoWrapper(LayerInfoImpl layerInfoImpl, String name, StyleInfo styleInfo, ResourceInfo resourceInfo) {
        layerInfoImpl.setId(generateID());
        layerInfoImpl.setType(PublishedType.VECTOR);
        layerInfoImpl.setResource(resourceInfo);
        layerInfoImpl.setDefaultStyle(styleInfo);
        layerInfoImpl.setEnabled(true);
        layerInfoImpl.setAdvertised(true);
        layerInfoImpl.setName(name);

        layerInfo = layerInfoImpl;
    }

    private static String generateID() {
        return "LayerInfoImpl--" + UUID.randomUUID().toString();
    }

    public LayerInfo getLayerInfo() {
        return layerInfo;
    }
}

package geoserver.cataloginfo.wrapper;

import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.impl.NamespaceInfoImpl;

import java.util.UUID;

public class NamespaceInfoWrapper {

    private NamespaceInfo namespaceInfo;

    public NamespaceInfoWrapper(NamespaceInfoImpl namespaceInfoImpl, String prefix, String uri) {
        namespaceInfoImpl.setId(generateID());
        namespaceInfoImpl.setPrefix(prefix);
        namespaceInfoImpl.setURI(uri);
        namespaceInfo = namespaceInfoImpl;
    }

    private static String generateID() {
        return "NamespaceInfoImpl--" + UUID.randomUUID().toString();
    }

    public NamespaceInfo getNamespace() {
        return namespaceInfo;
    }
}

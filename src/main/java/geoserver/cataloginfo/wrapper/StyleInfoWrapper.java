package geoserver.cataloginfo.wrapper;

import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.impl.StyleInfoImpl;

import java.util.UUID;

public class StyleInfoWrapper {

    private StyleInfo styleInfo;

    public StyleInfoWrapper(StyleInfoImpl styleInfoImpl, String name, String filename) {
        styleInfoImpl.setId(generateID());
        styleInfoImpl.setName(name);
        styleInfoImpl.setFilename(filename);
        styleInfoImpl.setFormat("sld");

        styleInfo = styleInfoImpl;
    }

    private static String generateID() {
        return "StyleInfoImpl--" + UUID.randomUUID().toString();
    }

    public StyleInfo getStyleInfo() {
        return styleInfo;
    }
}

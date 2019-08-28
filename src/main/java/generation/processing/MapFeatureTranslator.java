package generation.processing;

import model.styling.MapFeature;
import org.geotools.brewer.styling.builder.FeatureTypeStyleBuilder;
import org.geotools.brewer.styling.builder.StyleBuilder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapFeatureTranslator {

    private static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

    private MapFeatureTranslator() {
    }

    public static Collection<StyledLayerDescriptor> createStyledLayerDescriptorsFrom(Collection<MapFeature> mapFeatures) {
        List<StyledLayerDescriptor> styledLayerDescriptors = new ArrayList<>();

        mapFeatures.forEach(mapFeature -> styledLayerDescriptors.add(createSLDFor(mapFeature)));

        return styledLayerDescriptors;
    }

    private static StyledLayerDescriptor createSLDFor(MapFeature mapFeature) {
        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();
        NamedLayer layer = styleFactory.createNamedLayer();

        sld.setName(mapFeature.getName());
        mapFeature.getSubclasses().forEach((subclass -> {
            StyleBuilder styleBuilder = new StyleBuilder();
            FeatureTypeStyleBuilder featureTypeStyleBuilder = styleBuilder.featureTypeStyle();
            featureTypeStyleBuilder.name(subclass.getSubclassName());
            featureTypeStyleBuilder.rules(new ArrayList<>(subclass.getRules()));

            // IMPORTANT NOTE: rules that don't have a symbolizer set will get a Point symbolizer build by default
            // because point symbolizers apply to any kind of geometry (see https://docs.geoserver.org/latest/en/user/styling/sld/tipstricks/mixed-geometries.html)
            layer.addStyle(styleBuilder.buildStyle());
        }));

        sld.layers().add(layer);

        return sld;
    }
}

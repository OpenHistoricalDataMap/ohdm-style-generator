package generation.processing;

import model.styling.MapFeature;
import org.geotools.styling.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MapFeatureGeometrySplitter {

    private MapFeatureGeometrySplitter() {
    }

    public static Collection<MapFeature> splitMapFeaturesIntoGeometryTypes(Collection<MapFeature> mapFeatures) {
        Collection<MapFeature> splitMapFeatures = new ArrayList<>();

        mapFeatures.forEach(mapFeature -> {
            MapFeature pointMapFeature = createMapFeatureFrom(mapFeature, PointSymbolizerImpl.class);
            pointMapFeature.setName(mapFeature.getName() + "_points");
            MapFeature polygonMapFeature = createMapFeatureFrom(mapFeature, PolygonSymbolizerImpl.class);
            polygonMapFeature.setName(mapFeature.getName() + "_polygons");
            MapFeature lineMapFeature = createMapFeatureFrom(mapFeature, LineSymbolizerImpl.class);
            lineMapFeature.setName(mapFeature.getName() + "_lines");

            splitMapFeatures.add(pointMapFeature);
            splitMapFeatures.add(polygonMapFeature);
            splitMapFeatures.add(lineMapFeature);
        });

        return splitMapFeatures;
    }

    /**
     * This method will create a copy of the given MapFeature and only assign the symbolizers that match the given type
     * and TextSymbolizers.
     *
     * @param mapFeature The MapFeature that serves as the base
     * @param type       The type of the Symbolizers that have to be filtered and reassigned
     * @param <T>        The class of the type.
     * @return A new MapFeature that only has the rules from the base MapFeature that match the given type.
     */
    private static <T> MapFeature createMapFeatureFrom(MapFeature mapFeature, Class<T> type) {
        MapFeature copyOfMapFeature = new MapFeature(mapFeature);

        copyOfMapFeature.getSubclasses().forEach(mapFeatureSubclass -> {
            Collection<Rule> rulesForType = new ArrayList<>();

            mapFeatureSubclass.getRules().forEach(rule -> {
                List<Symbolizer> symbolizersOfType = rule.symbolizers().stream().filter(symbolizer -> type.isAssignableFrom(symbolizer.getClass())).collect(Collectors.toList());
                List<Symbolizer> textSymbolizers = rule.symbolizers().stream().filter(symbolizer -> symbolizer instanceof TextSymbolizer).collect(Collectors.toList());
                rule.symbolizers().clear();
                if (!symbolizersOfType.isEmpty()) {
                    rule.symbolizers().addAll(symbolizersOfType);
                    rule.symbolizers().addAll(textSymbolizers);
                    // if there are symbolizers for the specified type we want to add this rule to the mapfeaturesubclass
                    rulesForType.add(rule);
                }
            });

            // only add the rules that match the type, e.g. only add line rules to subclass with line type
            mapFeatureSubclass.setRules(rulesForType);
        });

        return copyOfMapFeature;
    }
}
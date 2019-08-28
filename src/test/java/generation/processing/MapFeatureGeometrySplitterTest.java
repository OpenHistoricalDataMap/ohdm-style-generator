package generation.processing;

import model.classification.ClassificationClass;
import model.classification.OSMClassification;
import model.styling.MapFeature;
import model.styling.MapFeatureSubclass;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MapFeatureGeometrySplitterTest {

    @Test
    void splitMapFeaturesIntoGeometryTypes() {
        Collection<MapFeature> mapFeatures = new ArrayList<>();
        OSMClassification classification = OSMClassification.getOSMClassification();
        mapFeatures = DefaultsFiller.fillRemaining(mapFeatures, classification);

        Collection<MapFeature> splitMapFeatures = MapFeatureGeometrySplitter.splitMapFeaturesIntoGeometryTypes(mapFeatures);

        assertNotNull(splitMapFeatures);
        assertEquals(3 * mapFeatures.size(), splitMapFeatures.size());

        List<String> mapFeatureNames = splitMapFeatures.stream().map(MapFeature::getName).collect(Collectors.toList());
        for (ClassificationClass classificationClass : classification.getClasses()) {
            assertTrue(mapFeatureNames.contains(classificationClass.getClassName() + "_lines"));
            assertTrue(mapFeatureNames.contains(classificationClass.getClassName() + "_points"));
            assertTrue(mapFeatureNames.contains(classificationClass.getClassName() + "_polygons"));
        }

        // check filtered symbolizers
        List<MapFeature> lineMapFeatures = splitMapFeatures.stream().filter(mapFeature -> mapFeature.getName().contains("lines")).collect(Collectors.toList());
        for (MapFeature lineMapFeature : lineMapFeatures) {
            for (MapFeatureSubclass subclass : lineMapFeature.getSubclasses()) {
                for (Rule rule : subclass.getRules()) {
                    assertEquals(1, rule.symbolizers().size());
                    assertTrue(rule.symbolizers().get(0) instanceof LineSymbolizer);
                }
            }
        }
        List<MapFeature> pointMapFeatures = splitMapFeatures.stream().filter(mapFeature -> mapFeature.getName().contains("points")).collect(Collectors.toList());
        for (MapFeature pointMapFeature : pointMapFeatures) {
            for (MapFeatureSubclass subclass : pointMapFeature.getSubclasses()) {
                for (Rule rule : subclass.getRules()) {
                    assertEquals(1, rule.symbolizers().size());
                    assertTrue(rule.symbolizers().get(0) instanceof PointSymbolizer);
                }
            }
        }
        List<MapFeature> polygonMapFeatures = splitMapFeatures.stream().filter(mapFeature -> mapFeature.getName().contains("polygons")).collect(Collectors.toList());
        for (MapFeature polygonMapFeature : polygonMapFeatures) {
            for (MapFeatureSubclass subclass : polygonMapFeature.getSubclasses()) {
                for (Rule rule : subclass.getRules()) {
                    assertEquals(1, rule.symbolizers().size());
                    assertTrue(rule.symbolizers().get(0) instanceof PolygonSymbolizer);
                }
            }
        }
    }
}
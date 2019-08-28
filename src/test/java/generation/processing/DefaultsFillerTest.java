package generation.processing;

import model.classification.Classification;
import model.classification.ClassificationClass;
import model.classification.ClassificationImpl;
import model.classification.OSMClassification;
import model.styling.MapFeature;
import model.styling.MapFeatureSubclass;
import org.geotools.styling.Rule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class DefaultsFillerTest {

    @Test
    void testFillRemainingWithEmptyList() {
        Classification classification = new ClassificationImpl();
        ArrayList<String> subclasses = new ArrayList<>();
        subclasses.add("undefined");
        subclasses.add("testsubclass");
        ClassificationClass testclass1 = new ClassificationClass("testclass1", subclasses);
        ClassificationClass testclass2 = new ClassificationClass("testclass2", subclasses);
        classification.addClassIfAbsent(testclass1);
        classification.addClassIfAbsent(testclass2);

        Collection<MapFeature> mapFeatures = new ArrayList<>();

        Collection<MapFeature> filledMapFeatures = DefaultsFiller.fillRemaining(mapFeatures, classification);

        assertTrue(filledMapFeatures.size() > mapFeatures.size());
        assertEquals(classification.getClasses().size(), filledMapFeatures.size());

        for (MapFeature filledMapFeature : filledMapFeatures) {
            for (MapFeatureSubclass subclass : filledMapFeature.getSubclasses()) {
                for (Rule rule : subclass.getRules()) {
                    assertNotNull(rule);
                    assertTrue(rule.symbolizers().size() > 0);
                }
            }
        }
    }

    @Test
    void testFillRemaining() {
        Classification classification = OSMClassification.getOSMClassification();

        Collection<MapFeature> mapFeatures = new ArrayList<>();
        mapFeatures.add(new MapFeature("aeroway"));
        mapFeatures.add(new MapFeature("boundary"));

        assertTrue(mapFeatures.size() > 0);

        Collection<MapFeature> filledMapFeatures = DefaultsFiller.fillRemaining(mapFeatures, classification);

        assertTrue(filledMapFeatures.size() > mapFeatures.size());
        assertEquals(classification.getClasses().size(), filledMapFeatures.size());

        for (MapFeature filledMapFeature : filledMapFeatures) {
            for (MapFeatureSubclass subclass : filledMapFeature.getSubclasses()) {
                for (Rule rule : subclass.getRules()) {
                    assertNotNull(rule);
                    assertTrue(rule.symbolizers().size() > 0);
                }
            }
        }
    }
}
package generation.processing;

import generation.parser.ConfigParseResult;
import generation.parser.ConfigParser;
import model.classification.Classification;
import model.classification.OSMClassification;
import model.styling.MapFeature;
import model.styling.MapFeatureSubclass;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapFeatureReferenceResolverTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");

    @Test
    void resolveMapFeatureReferences() {
        ConfigParseResult parseResult = createParseResult(Paths.get(resourceDirectory.toString(), "mapfeaturereferences", "valid_example.ohdmconfig").toFile());
        assertNotNull(parseResult);

        Classification classification = OSMClassification.getOSMClassification();
        MapFeatureReferenceResolver mapFeatureReferenceResolver = new MapFeatureReferenceResolver(classification);
        Collection<MapFeature> mapFeaturesWithResolvedReferences = mapFeatureReferenceResolver.resolveMapFeatureReferences(parseResult.getMapFeatures());

        assertNotNull(mapFeaturesWithResolvedReferences);

        MapFeature aerowayMapFeature = mapFeaturesWithResolvedReferences.stream().filter(mapFeature -> mapFeature.getName().equals("aeroway")).findFirst().get();
        assertNotNull(aerowayMapFeature);
        MapFeature militaryMapFeature = mapFeaturesWithResolvedReferences.stream().filter(mapFeature -> mapFeature.getName().equals("military")).findFirst().get();
        assertNotNull(militaryMapFeature);
        MapFeature waterwayMapFeature = mapFeaturesWithResolvedReferences.stream().filter(mapFeature -> mapFeature.getName().equals("waterway")).findFirst().get();
        assertNotNull(waterwayMapFeature);
        MapFeature ohdmBoundaryMapFeature = mapFeaturesWithResolvedReferences.stream().filter(mapFeature -> mapFeature.getName().equals("ohdm_boundary")).findFirst().get();
        assertNotNull(ohdmBoundaryMapFeature);

        MapFeatureSubclass undefinedAerowaySubClass = aerowayMapFeature.getSubclasses().stream().filter(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("undefined")).findFirst().get();
        MapFeatureSubclass undefinedMilitarySubClass = militaryMapFeature.getSubclasses().stream().filter(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("undefined")).findFirst().get();
        MapFeatureSubclass undefinedWaterwaySubClass = militaryMapFeature.getSubclasses().stream().filter(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("undefined")).findFirst().get();
        assertEquals(undefinedAerowaySubClass, undefinedMilitarySubClass);
        assertEquals(undefinedWaterwaySubClass, undefinedMilitarySubClass);
        assertEquals(undefinedWaterwaySubClass, undefinedAerowaySubClass);

        // assert ohdmBoundaryMapFeature has no subclasses because the 'undefined' subclass is not defined for it so it will not be copied
        assertEquals(0, ohdmBoundaryMapFeature.getSubclasses().size());
    }

    private ConfigParseResult createParseResult(File file) {
        try {
            return ConfigParser.parse(file);
        } catch (Exception e) {
            return null;
        }
    }
}
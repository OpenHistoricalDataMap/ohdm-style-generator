package generation.processing;

import generation.parser.ConfigParseResult;
import generation.parser.ConfigParser;
import model.styling.MapFeature;
import model.styling.MapFeatureSubclass;
import model.styling.StyleGroup;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlaceholderRuleResolverTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");

    private PlaceholderRuleResolver placeholderRuleResolver;

    @BeforeEach
    void setUp() throws IOException {
    }

    @Test
    void testResolvePlaceholderRulesWithValidReferences() {
        ConfigParseResult parseResult = createParseResult(Paths.get(resourceDirectory.toString(), "placeholderrules", "valid_example.ohdmconfig").toFile());
        assertNotNull(parseResult);
        Collection<MapFeature> mapFeatures = parseResult.getMapFeatures();
        Collection<StyleGroup> styleGroups = parseResult.getStyleGroups();

        Collection<MapFeature> mapFeaturesWithResolvedPlaceholderRules = PlaceholderRuleResolver.resolvePlaceholderRules(mapFeatures, styleGroups);
        assertNotNull(mapFeaturesWithResolvedPlaceholderRules);

        // check aeroway MapFeature
        MapFeature aerowayMapFeature = mapFeaturesWithResolvedPlaceholderRules.stream().findFirst().get();
        MapFeatureSubclass subclass = aerowayMapFeature.getSubclasses().stream().findFirst().get();

        // check SimplePolygon resolved
        Rule simplePolygon = subclass.getRules().stream().filter(rule -> rule.getName().equals("SimplePolygon")).findFirst().get();
        assertNotNull(simplePolygon);
        StyleGroup simplePolygonStyleGroup = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimplePolygon")).findFirst().get();
        assertEqualSymbolizers(simplePolygonStyleGroup.getRules().stream().findFirst().get().symbolizers(), simplePolygon.symbolizers());

        // check SimplePoint resolved
        Rule simplePoint = subclass.getRules().stream().filter(rule -> rule.getName().equals("SimplePoint")).findFirst().get();
        assertNotNull(simplePoint);
        StyleGroup simplePointStyleGroup = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimplePoint")).findFirst().get();
        assertEqualSymbolizers(simplePointStyleGroup.getRules().stream().findFirst().get().symbolizers(), simplePoint.symbolizers());

        // check SimpleLine resolved
        Rule simpleLine = subclass.getRules().stream().filter(rule -> rule.getName().equals("SimpleLine")).findFirst().get();
        assertNotNull(simpleLine);
        StyleGroup simpleLineStyleGroup = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimpleLine")).findFirst().get();
        assertEqualSymbolizers(simpleLineStyleGroup.getRules().stream().findFirst().get().symbolizers(), simpleLine.symbolizers());

        // check SimpleCombined
        StyleGroup simpleCombined = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimpleCombined")).findFirst().get();
        assertNotNull(simpleCombined);
        Rule simplePolygonRule = simpleCombined.getRules().stream().filter(rule -> rule.getName().equals("SimplePolygon")).findFirst().get();
        assertEqualSymbolizers(simplePolygonStyleGroup.getRules().stream().findFirst().get().symbolizers(), simplePolygonRule.symbolizers());
        Rule simplePointRule = simpleCombined.getRules().stream().filter(rule -> rule.getName().equals("SimplePoint")).findFirst().get();
        assertEqualSymbolizers(simplePointStyleGroup.getRules().stream().findFirst().get().symbolizers(), simplePointRule.symbolizers());
        Rule simpleLineRule = simpleCombined.getRules().stream().filter(rule -> rule.getName().equals("SimpleLine")).findFirst().get();
        assertEqualSymbolizers(simpleLineStyleGroup.getRules().stream().findFirst().get().symbolizers(), simpleLineRule.symbolizers());
    }

    private void assertEqualSymbolizers(List<Symbolizer> symbolizers1, List<Symbolizer> symbolizers2) {
        assertEquals(symbolizers1.size(), symbolizers2.size());
        for (int i = 0; i < symbolizers1.size(); i++) {
            assertEquals(symbolizers1.get(i), symbolizers2.get(i));
        }
    }

    private ConfigParseResult createParseResult(File file) {
        try {
            return ConfigParser.parse(file);
        } catch (Exception e) {
            return null;
        }
    }
}
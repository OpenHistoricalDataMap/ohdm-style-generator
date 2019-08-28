package generation.parser;

import model.styling.*;
import model.styling.zoom.ZoomRegion;
import org.geotools.styling.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources", "parser");
    private File sampleConfig = Paths.get(resourceDirectory.toString(), "example_simple.ohdmconfig").toFile();
    private File invalidConfig = Paths.get(resourceDirectory.toString(), "example_invalid.ohdmconfig").toFile();

    @Test
    void testParseWithComplexFile() {
        assertDoesNotThrow(() -> {
            ConfigParseResult result = ConfigParser.parse(sampleConfig);

            assertEquals(3, result.getMapFeatures().size());
            assertEquals(6, result.getStyleGroups().size());

            // -- AEROWAY -- //
            assertTrue(result.getMapFeatures().stream().anyMatch(mapFeature -> mapFeature.getName().equals("aeroway")));
            MapFeature aerowayMapFeature = result.getMapFeatures().stream().filter(mapFeature -> mapFeature.getName().equals("aeroway")).findFirst().get();
            assertNotNull(aerowayMapFeature);

            assertAerowaySubclasses(aerowayMapFeature);
            assertUndefinedSubclass(aerowayMapFeature);
            assertAerodromeSubclass(aerowayMapFeature);

            // -- MILITARY -- //
            MapFeature militaryMapFeature = result.getMapFeatures().stream().filter(mapFeature -> mapFeature.getName().equals("military")).findFirst().get();
            assertNotNull(militaryMapFeature);

            assertTrue(militaryMapFeature instanceof MapFeatureReference);
            assertEquals("military", militaryMapFeature.getName());
            assertEquals("aeroway", ((MapFeatureReference) militaryMapFeature).getNameOfReferencedMapFeature());

            // -- EMERGENCY -- //
            MapFeature emergencyMapFeature = result.getMapFeatures().stream().filter(mapFeature -> mapFeature.getName().equals("emergency")).findFirst().get();
            assertNotNull(emergencyMapFeature);

            assertTrue(emergencyMapFeature instanceof MapFeatureReference);
            assertEquals("emergency", emergencyMapFeature.getName());
            assertEquals("military", ((MapFeatureReference) emergencyMapFeature).getNameOfReferencedMapFeature());

            // -- STYLEGROUPS -- //
            assertStyleGroups(result.getStyleGroups());
        });
    }

    private void assertStyleGroups(Collection<StyleGroup> styleGroups) {
        StyleGroup simplePolygon = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimplePolygon")).findFirst().get();
        assertNotNull(simplePolygon);
        assertEquals(1, simplePolygon.getRules().size());
        Rule simplePolygonRule = simplePolygon.getRules().stream().findFirst().get();
        assertEquals(1, simplePolygonRule.symbolizers().size());
        Symbolizer simplePolygonRuleSymbolizer = simplePolygonRule.symbolizers().get(0);
        assertTrue(simplePolygonRuleSymbolizer instanceof PolygonSymbolizer);


        StyleGroup polygonWithStyledLabel = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("PolygonWithStyledLabel")).findFirst().get();
        assertNotNull(polygonWithStyledLabel);
        assertEquals(1, polygonWithStyledLabel.getRules().size());
        Rule polygonWithStyledLabelRule = polygonWithStyledLabel.getRules().stream().findFirst().get();
        assertEquals(2, polygonWithStyledLabelRule.symbolizers().size());
        Symbolizer polygonWithStyledLabelFirstSymbolizer = polygonWithStyledLabelRule.symbolizers().get(0);
        assertTrue(polygonWithStyledLabelFirstSymbolizer instanceof PolygonSymbolizer);
        Symbolizer polygonWithStyledLabelSecondSymbolizer = polygonWithStyledLabelRule.symbolizers().get(1);
        assertTrue(polygonWithStyledLabelSecondSymbolizer instanceof TextSymbolizer);


        StyleGroup simplePoint = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimplePoint")).findFirst().get();
        assertNotNull(simplePoint);
        assertEquals(1, simplePoint.getRules().size());
        Rule simplePointRule = simplePoint.getRules().stream().findFirst().get();
        assertEquals(1, simplePointRule.symbolizers().size());
        Symbolizer simplePointRuleSymbolizer = simplePointRule.symbolizers().get(0);
        assertTrue(simplePointRuleSymbolizer instanceof PointSymbolizer);


        StyleGroup pointWithLabel = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("PointWithLabel")).findFirst().get();
        assertNotNull(pointWithLabel);
        assertEquals(1, pointWithLabel.getRules().size());
        Rule pointWithLabelRule = pointWithLabel.getRules().stream().findFirst().get();
        assertEquals(2, pointWithLabelRule.symbolizers().size());
        Symbolizer pointWithLabelRuleFirstSymbolizer = pointWithLabelRule.symbolizers().get(0);
        assertTrue(pointWithLabelRuleFirstSymbolizer instanceof PointSymbolizer);
        Symbolizer pointWithLabelRuleSecondSymbolizer = pointWithLabelRule.symbolizers().get(1);
        assertTrue(pointWithLabelRuleSecondSymbolizer instanceof TextSymbolizer);


        StyleGroup simpleLine = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SimpleLine")).findFirst().get();
        assertNotNull(simpleLine);
        assertEquals(1, simpleLine.getRules().size());
        Rule simpleLineRule = simpleLine.getRules().stream().findFirst().get();
        assertEquals(1, simpleLineRule.symbolizers().size());
        Symbolizer simpleLineRuleSymbolizer = simpleLineRule.symbolizers().get(0);
        assertTrue(simpleLineRuleSymbolizer instanceof LineSymbolizer);

        StyleGroup spacedLineWithLabel = styleGroups.stream().filter(styleGroup -> styleGroup.getName().equals("SpacedLineWithLabel")).findFirst().get();
        assertNotNull(spacedLineWithLabel);
        assertEquals(1, spacedLineWithLabel.getRules().size());
        Rule spacedLineWithLabelRule = spacedLineWithLabel.getRules().stream().findFirst().get();
        assertEquals(2, spacedLineWithLabelRule.symbolizers().size());
        Symbolizer spacedLineWithLabelRuleFirstSymbolizer = spacedLineWithLabelRule.symbolizers().get(0);
        assertTrue(spacedLineWithLabelRuleFirstSymbolizer instanceof LineSymbolizer);
        Symbolizer spacedLineWithLabelRuleSecondSymbolizer = spacedLineWithLabelRule.symbolizers().get(1);
        assertTrue(spacedLineWithLabelRuleSecondSymbolizer instanceof TextSymbolizer);


    }

    private void assertAerowaySubclasses(MapFeature aerowayMapFeature) {
        assertTrue(aerowayMapFeature.getSubclasses().stream().anyMatch(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("undefined")));
        assertTrue(aerowayMapFeature.getSubclasses().stream().anyMatch(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("aerodrome")));
    }

    private void assertUndefinedSubclass(MapFeature aerowayMapFeature) {
        MapFeatureSubclass undefinedAeroway = aerowayMapFeature.getSubclasses().stream().filter(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("undefined")).findFirst().orElse(null);
        assertNotNull(undefinedAeroway);

        assertEquals(6, undefinedAeroway.getRules().size());
        Rule simplePolygonRule = undefinedAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SimplePolygon")).findAny().get();
        assertNotNull(simplePolygonRule);
        Rule simplePointRule = undefinedAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SimplePoint")).findAny().get();
        assertNotNull(simplePointRule);
        Rule simpleLineRule = undefinedAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SimpleLine")).findAny().get();
        assertNotNull(simpleLineRule);

        ZoomRegion defaultZoomRegion = ZoomRegion.getEmptyZoomRegion();
        assertZoomRegion(simplePolygonRule, defaultZoomRegion);
        assertZoomRegion(simplePointRule, defaultZoomRegion);
        assertZoomRegion(simpleLineRule, defaultZoomRegion);

        Rule polygonWithStyledLabelRule = undefinedAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("PolygonWithStyledLabel")).findAny().get();
        assertNotNull(polygonWithStyledLabelRule);
        Rule pointWithLabelRule = undefinedAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("PointWithLabel")).findAny().get();
        assertNotNull(pointWithLabelRule);
        Rule spacedLineWithLabel = undefinedAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SpacedLineWithLabel")).findAny().get();
        assertNotNull(spacedLineWithLabel);

        ZoomRegion secondZoomRegion = new ZoomRegion("0-10");
        assertZoomRegion(polygonWithStyledLabelRule, secondZoomRegion);
        assertZoomRegion(pointWithLabelRule, secondZoomRegion);
        assertZoomRegion(spacedLineWithLabel, secondZoomRegion);

    }

    private void assertAerodromeSubclass(MapFeature aerowayMapFeature) {
        MapFeatureSubclass aerodromeAeroway = aerowayMapFeature.getSubclasses().stream().filter(mapFeatureSubclass -> mapFeatureSubclass.getSubclassName().equals("aerodrome")).findFirst().orElse(null);
        assertNotNull(aerodromeAeroway);

        assertEquals(6, aerodromeAeroway.getRules().size());
        Rule simplePolygonRule = aerodromeAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SimplePolygon")).findAny().get();
        assertNotNull(simplePolygonRule);
        Rule simplePointRule = aerodromeAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SimplePoint")).findAny().get();
        assertNotNull(simplePointRule);
        Rule simpleLineRule = aerodromeAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SimpleLine")).findAny().get();
        assertNotNull(simpleLineRule);

        ZoomRegion firstZoomRegion = new ZoomRegion(">5");
        assertZoomRegion(simplePolygonRule, firstZoomRegion);
        assertZoomRegion(simplePointRule, firstZoomRegion);
        assertZoomRegion(simpleLineRule, firstZoomRegion);

        Rule polygonWithStyledLabelRule = aerodromeAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("PolygonWithStyledLabel")).findAny().get();
        assertNotNull(polygonWithStyledLabelRule);
        Rule pointWithLabelRule = aerodromeAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("PointWithLabel")).findAny().get();
        assertNotNull(pointWithLabelRule);
        Rule spacedLineWithLabel = aerodromeAeroway.getRules().stream().map(rule -> (PlaceholderRule) rule).filter(rule -> rule.getNameOfOriginal().equals("SpacedLineWithLabel")).findAny().get();
        assertNotNull(spacedLineWithLabel);

        ZoomRegion secondZoomRegion = new ZoomRegion("<5");
        assertZoomRegion(polygonWithStyledLabelRule, secondZoomRegion);
        assertZoomRegion(pointWithLabelRule, secondZoomRegion);
        assertZoomRegion(spacedLineWithLabel, secondZoomRegion);
    }

    private void assertZoomRegion(Rule rule, ZoomRegion zoomRegion) {
        assertEquals(zoomRegion.getMaxScaleDenominator(), rule.getMaxScaleDenominator());
        assertEquals(zoomRegion.getMinScaleDenominator(), rule.getMinScaleDenominator());
    }

    @Test
    void testParseWithInvalidFile() {
        File invalidFile = new File("asdf");
        assertThrows(IOException.class, () -> {
            ConfigParseResult parseResult = ConfigParser.parse(invalidFile);
        });
    }

    @Test
    void testParseWithInvalidInput() throws IOException {
        assertThrows(ParseException.class, () -> {
            ConfigParseResult parseResult = ConfigParser.parse(invalidConfig);
        });
    }

    @Test
    void testParseWithoutInput() {
        assertDoesNotThrow(() -> {
            ConfigParseResult result = ConfigParser.parse("");
            assertEquals(0, result.getMapFeatures().size());
            assertEquals(0, result.getStyleGroups().size());
        });
    }
}
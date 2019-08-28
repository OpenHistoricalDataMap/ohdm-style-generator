package generation.processing;

import generation.parser.ConfigParseResult;
import generation.parser.ConfigParser;
import model.classification.Classification;
import model.classification.OSMClassification;
import org.geotools.styling.StyledLayerDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ParseResultProcessorTest {

    private ParseResultProcessor parseResultProcessor;
    private Classification classification;

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File sampleConfig = Paths.get(resourceDirectory.toString(), "parser/example_simple.ohdmconfig").toFile();


    @BeforeEach
    void setup() {
        classification = OSMClassification.getOSMClassification();
        parseResultProcessor = new ParseResultProcessor(classification);
    }

    @Test
    void getSLDFromValidParseResultWithDefaults() throws IOException {
        ConfigParseResult parseResult = ConfigParser.parse(sampleConfig);

        Collection<StyledLayerDescriptor> styledLayerDescriptorsFromParseResult = parseResultProcessor.getStyledLayerDescriptorsFromParseResult(parseResult, true);

        assertNotNull(styledLayerDescriptorsFromParseResult);
        assertEquals(classification.getClasses().size() * 3, styledLayerDescriptorsFromParseResult.size());
    }

    @Test
    void getSLDFromValidParseResultWithoutDefaults() throws IOException {
        ConfigParseResult parseResult = ConfigParser.parse(sampleConfig);

        Collection<StyledLayerDescriptor> styledLayerDescriptorsFromParseResult = parseResultProcessor.getStyledLayerDescriptorsFromParseResult(parseResult, false);

        assertNotNull(styledLayerDescriptorsFromParseResult);
        assertTrue(classification.getClasses().size() >= styledLayerDescriptorsFromParseResult.size());
    }

    @Test
    void getSLDFromEmptyParseResultWithDefaults() throws IOException {
        ConfigParseResult parseResult = new ConfigParseResult(new ArrayList<>(), new ArrayList<>());

        Collection<StyledLayerDescriptor> styledLayerDescriptorsFromParseResult = parseResultProcessor.getStyledLayerDescriptorsFromParseResult(parseResult, true);

        assertNotNull(styledLayerDescriptorsFromParseResult);
        assertEquals(classification.getClasses().size() * 3, styledLayerDescriptorsFromParseResult.size());
    }

    @Test
    void getSLDFromEmptyParseResultWithoutDefaults() throws IOException {
        ConfigParseResult parseResult = new ConfigParseResult(new ArrayList<>(), new ArrayList<>());

        Collection<StyledLayerDescriptor> styledLayerDescriptorsFromParseResult = parseResultProcessor.getStyledLayerDescriptorsFromParseResult(parseResult, false);

        assertNotNull(styledLayerDescriptorsFromParseResult);
        assertEquals(0, styledLayerDescriptorsFromParseResult.size());
    }
}
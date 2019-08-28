package testutils;

import generation.parser.ConfigParseResult;
import generation.parser.ConfigParser;
import generation.processing.ParseResultProcessor;
import model.classification.OSMClassification;
import org.geotools.styling.StyledLayerDescriptor;
import util.SLDWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class TestUtils {

    private static Path resourceDirectory = Paths.get("src", "test", "resources");
    private static File sampleConfig = Paths.get(resourceDirectory.toString(), "parser/example_simple.ohdmconfig").toFile();

    public static void generateSampleSLDsIntoDirectory(File targetDirectory) throws IOException {
        ConfigParseResult parseResult = ConfigParser.parse(sampleConfig);

        Collection<StyledLayerDescriptor> styledLayerDescriptorsFromParseResult = new ParseResultProcessor(OSMClassification.getOSMClassification())
                .getStyledLayerDescriptorsFromParseResult(parseResult, true);

        writeSLDsToFiles(styledLayerDescriptorsFromParseResult, targetDirectory);
    }

    private static void writeSLDsToFiles(Collection<StyledLayerDescriptor> styledLayerDescriptors, File targetDirectory) {
        styledLayerDescriptors.forEach(sld -> {
            SLDWrapper sldWrapper = new SLDWrapper(sld);
            sldWrapper.writeFileTo(targetDirectory);
        });
    }
}

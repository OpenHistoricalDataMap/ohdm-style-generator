package generation.processing;

import generation.parser.ConfigParseResult;
import model.classification.Classification;
import model.styling.MapFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.styling.StyledLayerDescriptor;

import java.util.Collection;

public class ParseResultProcessor {

    private final Logger logger = LogManager.getLogger(ParseResultProcessor.class);

    private Classification classification;

    public ParseResultProcessor(Classification classification) {
        this.classification = classification;
        mapFeatureReferenceResolver = new MapFeatureReferenceResolver(classification);
    }

    private MapFeatureReferenceResolver mapFeatureReferenceResolver;

    public Collection<StyledLayerDescriptor> getStyledLayerDescriptorsFromParseResult(ConfigParseResult configParseResult, boolean generateDefaults) {
        Collection<MapFeature> mapFeatures = configParseResult.getMapFeatures();

        logger.info("Starting processing of the following parse result: \n\t {}.", configParseResult);

        // order is important
        logger.info("Resolving references for PlaceholderRules...");
        mapFeatures = PlaceholderRuleResolver.resolvePlaceholderRules(mapFeatures, configParseResult.getStyleGroups());
        logger.info("Resolving references between MapFeatures...");
        mapFeatures = mapFeatureReferenceResolver.resolveMapFeatureReferences(mapFeatures);

        if (generateDefaults) {
            logger.info("Creating MapFeatures with default values for non-declared classes from classification...");
            mapFeatures = DefaultsFiller.fillRemaining(mapFeatures, classification);
        }

        logger.info("Splitting MapFeatures into geometry types...");
        mapFeatures = MapFeatureGeometrySplitter.splitMapFeaturesIntoGeometryTypes(mapFeatures);

        logger.info("Transforming MapFeatures to SLDs...");
        return MapFeatureTranslator.createStyledLayerDescriptorsFrom(mapFeatures);
    }

    // Logs statements about MapFeatures that don't exist in the classification
    private void compareParseResultToClassification(ConfigParseResult configParseResult) {
    }
}

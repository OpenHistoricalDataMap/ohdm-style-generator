package generation.processing;

import model.classification.Classification;
import model.classification.ClassificationClass;
import model.classification.OSMClassification;
import model.styling.MapFeature;
import org.geotools.styling.NamedLayerImpl;
import org.geotools.styling.StyledLayerDescriptor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MapFeatureTranslatorTest {

    @Test
    void createStyledLayerDescriptorsFrom() {
        Collection<MapFeature> mapFeatures = new ArrayList<>();
        Classification classification = OSMClassification.getOSMClassification();
        mapFeatures = DefaultsFiller.fillRemaining(mapFeatures, classification);
        Collection<StyledLayerDescriptor> styledLayerDescriptors = MapFeatureTranslator.createStyledLayerDescriptorsFrom(mapFeatures);

        assertNotNull(styledLayerDescriptors);
        assertEquals(classification.getClasses().size(), styledLayerDescriptors.size());

        // assert that there is a sld for every classificationClass
        for (ClassificationClass classificationClass : classification.getClasses()) {
            List<String> sldNames = styledLayerDescriptors.stream().map(StyledLayerDescriptor::getName).collect(Collectors.toList());
            assertTrue(sldNames.contains(classificationClass.getClassName()));
        }

        // assert that every sld has the same amount of styles in their layer as the classification has subclasses defined for them
        for (StyledLayerDescriptor styledLayerDescriptor : styledLayerDescriptors) {
            ClassificationClass matchingClass = classification.getClasses().stream().filter(classificationClass -> classificationClass.getClassName().equals(styledLayerDescriptor.getName())).findFirst().get();
            NamedLayerImpl styledLayer = (NamedLayerImpl) styledLayerDescriptor.getStyledLayers()[0];
            assertEquals(matchingClass.getSubclassNames().size(), styledLayer.styles().size());
        }
    }
}
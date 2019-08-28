package generation.processing;

import model.classification.Classification;
import model.classification.ClassificationClass;
import model.styling.MapFeature;
import model.styling.MapFeatureSubclass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.brewer.styling.builder.MarkBuilder;
import org.geotools.brewer.styling.builder.PolygonSymbolizerBuilder;
import org.geotools.brewer.styling.builder.RuleBuilder;
import org.geotools.brewer.styling.builder.StrokeBuilder;
import org.geotools.styling.Rule;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class DefaultsFiller {

    private static final Logger logger = LogManager.getLogger(DefaultsFiller.class);

    private DefaultsFiller() {
    }

    public static Collection<MapFeature> fillRemaining(Collection<MapFeature> mapFeatures, Classification classification) {
        Collection<MapFeature> copyOfMapFeatures = new ArrayList<>(mapFeatures);

        Collection<String> classNames = classification.getClasses().stream().map(ClassificationClass::getClassName).collect(Collectors.toList());
        Collection<String> usedClassNames = copyOfMapFeatures.stream().map(MapFeature::getName).collect(Collectors.toList());

        // this results in a collection with the unused classnames
        classNames.removeAll(usedClassNames);
        classNames.forEach(className -> {
            MapFeature fillerMapFeatureWithName = createFillerMapFeatureWithName(className, classification);
            logger.info("Created default style for MapFeature {}.", className);
            copyOfMapFeatures.add(fillerMapFeatureWithName);
        });

        return copyOfMapFeatures;
    }

    private static MapFeature createFillerMapFeatureWithName(String name, Classification classification) {
        MapFeature mapFeature = new MapFeature(name);

        Collection<String> subClassesForClass = classification.getClass(name).getSubclassNames();
        subClassesForClass.forEach(subclass -> mapFeature.addSubclass(new MapFeatureSubclass(subclass, createDefaultRules(subclass))));

        return mapFeature;
    }

    private static Collection<Rule> createDefaultRules(String subclass) {
        Collection<Rule> rules = new ArrayList<>();
        RuleBuilder ruleBuilder = new RuleBuilder();

        MarkBuilder markBuilder = ruleBuilder.point().graphic().mark();
        markBuilder.name("circle")
                .fill()
                .color(Color.BLACK)
                .build();

        PolygonSymbolizerBuilder polygonSymbolizerBuilder = ruleBuilder.polygon();
        polygonSymbolizerBuilder
                .fill()
                .color(Color.BLACK)
                .build();

        StrokeBuilder strokeBuilder = ruleBuilder.line().stroke();
        strokeBuilder
                .color(Color.BLACK)
                .width(1)
                .build();

        ruleBuilder.filter("subclassname = '" + subclass + "'");
        Rule rule = ruleBuilder.build();
        rules.add(rule);
        return rules;
    }
}

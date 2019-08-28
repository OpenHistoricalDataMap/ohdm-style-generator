package generation.processing;

import model.classification.Classification;
import model.classification.ClassificationClass;
import model.styling.MapFeature;
import model.styling.MapFeatureReference;
import model.styling.MapFeatureSubclass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class MapFeatureReferenceResolver {

    private final Logger logger = LogManager.getLogger(MapFeatureReferenceResolver.class);
    private static final int MAX_RESOLVING_ATTEMPTS = 10;

    private Collection<MapFeature> mapFeatures;
    private Classification classification;

    public MapFeatureReferenceResolver(Classification classification) {
        this.classification = classification;
    }

    /**
     * The resolving of MapFeatureReferences has to run a loop because it might be that there are references
     * to other MapFeatureReferences so they need to be resolved first.
     */
    public Collection<MapFeature> resolveMapFeatureReferences(Collection<MapFeature> mapFeatures) {
        // copy mapfeatures to prevent side effects on original collection
        this.mapFeatures = new ArrayList<>(mapFeatures);

        repeatedlyTryResolving(MAX_RESOLVING_ATTEMPTS);
        return this.mapFeatures;
    }

    private void repeatedlyTryResolving(int maxAttempts) {
        int exitCounter = maxAttempts;

        Collection<MapFeatureReference> mapFeatureReferences;
        do {
            mapFeatureReferences = getMapFeatureReferences();

            Iterator<MapFeatureReference> iterator = mapFeatureReferences.iterator();
            while (iterator.hasNext()) {
                MapFeatureReference next = iterator.next();
                try {
                    boolean replacementSuccessful = replaceReferenceWithMapFeature(next);
                    if (!replacementSuccessful) {
                        exitCounter--;
                    }
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage());
                    iterator.remove();
                }
            }

        } while (!mapFeatureReferences.isEmpty() && exitCounter > 0);
    }

    private Collection<MapFeatureReference> getMapFeatureReferences() {
        return mapFeatures.stream()
                .filter(mapFeature -> mapFeature instanceof MapFeatureReference)
                .map(mapFeature -> (MapFeatureReference) mapFeature)
                .collect(Collectors.toList());
    }

    /**
     * @param mapFeatureReference
     * @return true if the replacement was successful
     */
    private boolean replaceReferenceWithMapFeature(MapFeatureReference mapFeatureReference) throws IllegalArgumentException {
        String nameOfReferencedMapFeature = mapFeatureReference.getNameOfReferencedMapFeature();
        MapFeature mapFeatureWithName = findMapFeatureWithName(nameOfReferencedMapFeature);

        if (mapFeatureWithName instanceof MapFeatureReference) {
            // found the mapfeature but it is just a reference
            return false;
        }
        if (mapFeatureWithName != null) {
            MapFeature mapFeatureReferenceReplacementFor = createMapFeatureReferenceReplacementFor(mapFeatureReference, mapFeatureWithName);
            mapFeatures.add(mapFeatureReferenceReplacementFor);
            mapFeatures.remove(mapFeatureReference);
            return true;
        } else {
            throw new IllegalArgumentException("MapFeature " + mapFeatureReference.getName() + " is referencing non-existent MapFeature " + mapFeatureReference.getNameOfReferencedMapFeature());
        }
    }

    private MapFeature createMapFeatureReferenceReplacementFor(MapFeatureReference mapFeatureReference, MapFeature baseMapFeature) {
        Collection<MapFeatureSubclass> subclasses = getSubclassesMatchingClassificationFor(mapFeatureReference.getName(), baseMapFeature.getSubclasses());
        return new MapFeature(mapFeatureReference.getName(), subclasses);
    }

    private Collection<MapFeatureSubclass> getSubclassesMatchingClassificationFor(String className, Collection<MapFeatureSubclass> subclasses) {
        ClassificationClass classificationClass = classification.getClass(className);
        Collection<String> subclassNames = classificationClass.getSubclassNames();

        List<MapFeatureSubclass> mapFeatureSubclasses = subclasses.stream()
                .filter(mapFeatureSubclass -> subclassNames.contains(mapFeatureSubclass.getSubclassName()))
                .collect(Collectors.toList());

        return mapFeatureSubclasses;
    }

    @Nullable
    private MapFeature findMapFeatureWithName(String name) {
        List<MapFeature> mapFeaturesWithName = mapFeatures.stream()
                .filter(mapFeature -> mapFeature.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
        if (!mapFeaturesWithName.isEmpty()) {
            return mapFeaturesWithName.get(0);
        } else {
            return null;
        }
    }
}

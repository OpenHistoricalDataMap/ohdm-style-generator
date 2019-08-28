package model.styling;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MapFeatureReference extends MapFeature implements Placeholder {

    private String nameOfReferencedMapFeature;

    /**
     * Use the placeholderName as subclassName.
     *
     * @param mapFeatureName         name of the MapFeature this object is referencing
     */
    public MapFeatureReference(@NotNull String mapFeatureName, String nameOfReferencedMapFeature) {
        super(mapFeatureName, new ArrayList<>());
        this.nameOfReferencedMapFeature = nameOfReferencedMapFeature;
    }

    public void setNameOfReferencedMapFeature(String nameOfReferencedMapFeature) {
        this.nameOfReferencedMapFeature = nameOfReferencedMapFeature;
    }

    public String getNameOfReferencedMapFeature() {
        return nameOfReferencedMapFeature;
    }

}

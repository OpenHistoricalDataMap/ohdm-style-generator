package generation.parser;

import model.styling.MapFeature;
import model.styling.StyleGroup;

import java.util.Collection;
import java.util.stream.Collectors;

public class ConfigParseResult {

    private Collection<MapFeature> mapFeatures;
    private Collection<StyleGroup> styleGroups;

    public ConfigParseResult(Collection<MapFeature> mapFeatures, Collection<StyleGroup> styleGroups) {
        this.mapFeatures = mapFeatures;
        this.styleGroups = styleGroups;
    }

    public boolean addMapFeature(MapFeature mapFeature) {
        return mapFeatures.add(mapFeature);
    }

    public boolean addStyleGroup(StyleGroup styleGroup) {
        return styleGroups.add(styleGroup);
    }

    public Collection<MapFeature> getMapFeatures() {
        return mapFeatures;
    }

    public Collection<StyleGroup> getStyleGroups() {
        return styleGroups;
    }

    @Override
    public String toString() {
        return "ConfigParseResult{" +
                "mapFeatures=" + mapFeatures.stream().map(MapFeature::getName).collect(Collectors.joining(", ")) +
                ", styleGroups=" + styleGroups.stream().map(StyleGroup::getName).collect(Collectors.joining(", ")) +
                '}';
    }
}

package model.styling;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MapFeature {

    @NotNull
    private String name;
    @NotNull
    private Collection<MapFeatureSubclass> subclasses = new HashSet<>();

    public MapFeature(@NotNull String name) {
        this.name = name;
    }

    public MapFeature(@NotNull String name, @NotNull Collection<MapFeatureSubclass> subclasses) {
        this.name = name;
        this.subclasses = subclasses;
    }

    public MapFeature(MapFeature mapFeature) {
        this.name = mapFeature.name;

        mapFeature.getSubclasses().forEach(mapFeatureSubclass -> {
            this.subclasses.add(new MapFeatureSubclass(mapFeatureSubclass));
        });
    }

    public boolean addSubclass(MapFeatureSubclass subclass) {
        return subclasses.add(subclass);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Collection<MapFeatureSubclass> getSubclasses() {
        return subclasses;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setSubclasses(@NotNull Collection<MapFeatureSubclass> subclasses) {
        this.subclasses = subclasses;
    }

    @Override
    public String toString() {
        return "MapFeature{" +
                "name='" + name + '\'' +
                ", subclasses=" + subclasses +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapFeature)) return false;
        MapFeature that = (MapFeature) o;
        return name.equals(that.name) &&
                subclasses.equals(that.subclasses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subclasses);
    }
}

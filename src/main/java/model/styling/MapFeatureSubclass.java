package model.styling;

import com.google.gson.Gson;
import org.geotools.styling.Rule;
import org.geotools.styling.RuleImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class MapFeatureSubclass {

    public MapFeatureSubclass(@NotNull String subclassName, @NotNull Collection<Rule> rules) {
        this.subclassName = subclassName;
        this.rules = rules;
    }

    @NotNull
    private String subclassName;
    @NotNull
    private Collection<Rule> rules = new ArrayList<>();

    public MapFeatureSubclass(MapFeatureSubclass mapFeatureSubclass) {
        this.subclassName = mapFeatureSubclass.getSubclassName();
        mapFeatureSubclass.getRules().forEach(rule -> {
            if (rule instanceof PlaceholderRule) {
                this.rules.add(new PlaceholderRule((PlaceholderRule) rule));
            } else {
                this.rules.add(new RuleImpl(rule));
            }
        });
    }

    @NotNull
    public String getSubclassName() {
        return subclassName;
    }

    @NotNull
    public Collection<Rule> getRules() {
        return rules;
    }

    public void setRules(@NotNull Collection<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "MapFeatureSubclass{" +
                "subclassName='" + subclassName + '\'' +
                ", rules=" + rules +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapFeatureSubclass)) return false;
        MapFeatureSubclass that = (MapFeatureSubclass) o;
        return subclassName.equals(that.subclassName) &&
                rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subclassName, rules);
    }
}

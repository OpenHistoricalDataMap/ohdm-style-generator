package model.styling;

import org.geotools.styling.Rule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


public class StyleGroup {

    @NotNull
    private String name;
    @NotNull
    private Collection<Rule> rules;

    public StyleGroup(@NotNull String name, @Nullable Collection<Rule> rules) {
        this.name = name;
        this.rules = rules;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Collection<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        return "StyleGroup{" +
                "name='" + name + '\'' +
                ", style=" + rules +
                '}';
    }
}

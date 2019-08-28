package model.styling;


import org.geotools.styling.Rule;
import org.geotools.styling.RuleImpl;

import java.util.Objects;

public class PlaceholderRule extends RuleImpl implements Placeholder {

    private String nameOfActualStyle;

    public PlaceholderRule(String styleName) {
        nameOfActualStyle = styleName;
        setName(styleName);
    }

    public PlaceholderRule(PlaceholderRule rule) {
        nameOfActualStyle = rule.nameOfActualStyle;
        setName(rule.getName());
    }

    public String getNameOfOriginal() {
        return nameOfActualStyle;
    }

    public void setNameOfActualStyle(String nameOfActualStyle) {
        this.nameOfActualStyle = nameOfActualStyle;
    }

    @Override
    public String toString() {
        return "PlaceholderRule{" +
                "nameOfActualStyle='" + nameOfActualStyle + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceholderRule)) return false;
        if (!super.equals(o)) return false;
        PlaceholderRule that = (PlaceholderRule) o;
        return Objects.equals(nameOfActualStyle, that.nameOfActualStyle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nameOfActualStyle);
    }
}

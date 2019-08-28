package model.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ClassificationClass {

    private String className;
    private Collection<String> subclassNames;

    private Collection<String> geometryTypes;

    public ClassificationClass(String className) {
        this.className = className;
        subclassNames = new ArrayList<>();
    }

    public ClassificationClass(String className, Collection<String> subclassNames) {
        this.className = className;
        this.subclassNames = subclassNames;
    }

    public boolean hasSubClass(String subclassName) {
        return subclassNames.contains(subclassName);
    }

    public void addSubClass(String subclassName) {
        if (!subclassNames.contains(subclassName))
            subclassNames.add(subclassName);
    }

    public String getClassName() {
        return className;
    }

    public void setGeometryTypes(Collection<String> geometryTypes) {
        this.geometryTypes = geometryTypes;
    }

    /**
     * @return extended classnames (e.g. aeroway_lines, aeroway_polygons, ...) or the default classname if no geometry types are set
     */
    public Collection<String> getExtendedClassNames() {
        Collection<String> extendedClassNames = new ArrayList<>();
        if (geometryTypes != null) {
            for (String geometryType : geometryTypes) {
                extendedClassNames.add(className + "_" + geometryType);
            }
        } else {
            extendedClassNames.add(getClassName());
        }
        return extendedClassNames;
    }

    public Collection<String> getSubclassNames() {
        return subclassNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassificationClass)) return false;
        ClassificationClass that = (ClassificationClass) o;
        return Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
}

package model.classification;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class ClassificationImpl implements Classification {

    private Collection<ClassificationClass> classes = new HashSet<>();

    private final Collection<String> DEFAULT_GEOMETRY_TYPES = Arrays.asList("points", "lines", "polygons");
    private Collection<String> geometryTypes;

    public ClassificationImpl() {
        geometryTypes = DEFAULT_GEOMETRY_TYPES;
    }

    public ClassificationImpl(Collection<String> geometryTypes) {
        this.geometryTypes = geometryTypes;
    }

    @Override
    public Collection<ClassificationClass> getClasses() {
        return classes;
    }

    @Override
    public ClassificationClass getClass(String className) {
        return classes.stream().filter(cc -> cc.getClassName().equals(className)).findAny().orElse(null);
    }

    @Override
    public Collection<String> getSubClassesForClass(ClassificationClass classificationClass) {
        ClassificationClass matchingClass = classes.stream().findAny().filter(cc -> cc.equals(classificationClass)).orElse(null);
        if (matchingClass != null) {
            return matchingClass.getSubclassNames();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasClass(ClassificationClass classificationClass) {
        return classes.stream().findAny().filter(cc -> cc.equals(classificationClass)).isPresent();
    }

    @Override
    public void addClassIfAbsent(ClassificationClass classificationClass) {
        if (!classes.contains(classificationClass)) {
            classificationClass.setGeometryTypes(geometryTypes);
            classes.add(classificationClass);
        }
    }
}

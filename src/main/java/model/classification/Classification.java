package model.classification;

import java.util.Collection;

public interface Classification {

    Collection<ClassificationClass> getClasses();

    ClassificationClass getClass(String className);

    Collection<String> getSubClassesForClass(ClassificationClass classificationClass);

    boolean hasClass(ClassificationClass classificationClass);

    void addClassIfAbsent(ClassificationClass classificationClass);
}

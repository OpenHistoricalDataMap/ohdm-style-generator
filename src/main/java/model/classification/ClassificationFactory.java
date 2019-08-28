package model.classification;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClassificationFactory {

    public static Classification getDefaultClassification() {
        return OSMClassification.getOSMClassification();
    }

    public static Classification getClassificationFromDataSource(DataSource dataSource) throws SQLException {
        Classification classification = new ClassificationImpl();

        try (Connection connection = dataSource.getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM classification")) {

            while (rs.next()) {
                String className = rs.getString("class");
                ClassificationClass classificationClass = classification.getClass(className);
                if (classificationClass == null) {
                    classificationClass = new ClassificationClass(className);
                    classification.addClassIfAbsent(classificationClass);
                }

                String subClassName = rs.getString("subclassname");
                classificationClass.addSubClass(subClassName);
            }
        }

        return classification;
    }
}

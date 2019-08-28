package db;

import model.classification.OSMClassification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * You have to place a file called datasource-config.json with valid connection properties in src/test/resources/.env
 * in order for this test to work.
 */
class DatabaseConfiguratorTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File validDatasourceConfigFile = Paths.get(resourceDirectory.toString(), ".env/datasource-config.json").toFile();

    @Test
    void testCreateTablesForClassificationWithValidDataSource() throws FileNotFoundException, SQLException {
        DataSourceConfig dataSourceConfig = DataSourceFactory.createDataSourceConfigFromJSON(validDatasourceConfigFile);
        DataSourceWrapper dataSourceWrapper = DataSourceFactory.createDataSourceWrapperFrom(dataSourceConfig);
        DatabaseConfigurator databaseConfigurator = new DatabaseConfigurator(dataSourceWrapper);
        OSMClassification classification = OSMClassification.getOSMClassification();

        databaseConfigurator.createTablesForClassification(classification);

        try (Connection connection = dataSourceWrapper.getConnection()) {
            Collection<String> existingTableNames = getExistingTableNames(connection, dataSourceWrapper.getSchema());


            List<String> classificationClassNames = new ArrayList<>();
            classification.getClasses().forEach(classificationClass -> {
                classificationClassNames.addAll(classificationClass.getExtendedClassNames());
            });

            // assert that a table exists for every extended classificationClass name
            for (String classificationClassName : classificationClassNames) {
                Assertions.assertTrue(existingTableNames.contains(classificationClassName));
            }
        }
    }

    private Collection<String> getExistingTableNames(Connection connection, String schema) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, schema, null, new String[]{"TABLE"});

        Collection<String> tableNames = new ArrayList<>();
        while (resultSet.next()) {
            tableNames.add(resultSet.getString("TABLE_NAME"));
        }
        return tableNames;
    }
}
package db;

import model.classification.Classification;
import model.classification.ClassificationClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class DatabaseConfigurator {

    private String universalSQL = "CREATE TABLE SCHEMA_PLACEHOLDER.TABLENAME_PLACEHOLDER (" +
            "geom geometry NULL," +
            "object_id int8 NULL," +
            "geom_id int8 NULL," +
            "subclassname varchar NULL," +
            "\"name\" varchar NULL," +
            "valid_since date NULL," +
            "valid_until date NULL," +
            "valid_since_offset int8 NULL," +
            "valid_until_offset int8 NULL" +
            ");";

    private final Logger logger = LogManager.getLogger(DatabaseConfigurator.class);
    private final DataSourceWrapper dataSourceWrapper;

    public DatabaseConfigurator(DataSourceWrapper dataSourceWrapper) {
        this.dataSourceWrapper = dataSourceWrapper;
    }

    public void createTablesForClassification(@NotNull Classification classification) throws SQLException {
        Collection<ClassificationClass> classes = classification.getClasses();

        String selectedSchema = dataSourceWrapper.getSchema();
        // IMPORTANT: set schema to public to get access to PostGIS extension
        dataSourceWrapper.setSchema("public");

        try (Connection connection = dataSourceWrapper.getConnection();
             Statement statement = connection.createStatement()) {

            Collection<String> existingTableNames = getExistingTableNames(connection, selectedSchema);
            for (ClassificationClass classificationClass : classes) {
                for (String extendedClassName : classificationClass.getExtendedClassNames()) {
                    createSingleTableForClass(statement, existingTableNames, extendedClassName, selectedSchema);
                }
            }
            logger.info("Setup of tables matching classification done.");
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

    private void createSingleTableForClass(Statement statement, Collection<String> existingTableNames, String classname, String schema) throws SQLException {
        if (!existingTableNames.contains(classname)) {
            executeTableCreationForClass(statement, classname, schema);
            logger.info("Created table for {} in schema {}", classname, schema);
        }
    }

    private void executeTableCreationForClass(Statement statement, String classname, String schema) throws SQLException {
        String sqlStatement = replacePlaceholdersInSQLString(universalSQL, classname, schema);
        statement.executeUpdate(sqlStatement);
    }

    private String replacePlaceholdersInSQLString(String sql, String tableName, String schema) {
        return sql.replace("SCHEMA_PLACEHOLDER", schema).replace("TABLENAME_PLACEHOLDER", tableName);

    }
}

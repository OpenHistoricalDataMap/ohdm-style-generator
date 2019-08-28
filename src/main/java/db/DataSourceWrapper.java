package db;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wraps a PGSimpleDataSource because the DataSource interface itself does not provide methods to change the schema.
 * You would usually set the schema you want and call getConnection() afterwards to establish a new connection that uses the schema.
 */
public class DataSourceWrapper {

    private PGSimpleDataSource dataSource;

    public DataSourceWrapper(DataSourceConfig data) {
        dataSource = new PGSimpleDataSource();
        dataSource.setServerName(data.getHost());
        dataSource.setDatabaseName(data.getDatabase());
        dataSource.setPortNumber(data.getPort());
        dataSource.setUser(data.getUser());
        dataSource.setPassword(data.getPassword());
        dataSource.setCurrentSchema(data.getSchema());
    }

    public String getSchema() {
        return dataSource.getCurrentSchema();
    }

    public void setSchema(String newSchema) {
        dataSource.setCurrentSchema(newSchema);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

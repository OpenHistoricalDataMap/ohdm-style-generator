package db;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DataSourceFactory {

    public static DataSourceConfig createDataSourceConfigFromJSON(File file) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(file));
        return gson.fromJson(reader, DataSourceConfig.class);
    }

    public static DataSourceWrapper createDataSourceWrapperFrom(DataSourceConfig dataSourceConfig) {
        return new DataSourceWrapper(dataSourceConfig);
    }

    public static DataSource createDataSourceFrom(DataSourceConfig dataSourceConfig) {
        DataSourceWrapper wrapper = createDataSourceWrapperFrom(dataSourceConfig);
        return wrapper.getDataSource();
    }
}

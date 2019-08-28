package geoserver.connection;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import db.DataSourceConfig;
import org.geoserver.catalog.NamespaceInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServerConnectionFactory {

    public static ServerConnectionConfig createConnectionConfigFromJSON(File configFile) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(configFile));
        return gson.fromJson(reader, ServerConnectionConfig.class);
    }

    public static Map<String, Serializable> getConnectionParametersFrom(DataSourceConfig config, NamespaceInfo namespaceInfo) {
        Map<String, Serializable> parameters = new HashMap<>();

        // IMPORTANT: this is needed for the lookup and has to match
        parameters.put("namespace", namespaceInfo.getURI());

        parameters.put("host", config.getHost());
        parameters.put("port", config.getPort());
        parameters.put("database", config.getDatabase());
        parameters.put("schema", config.getSchema());
        parameters.put("user", config.getUser());
        parameters.put("passwd", config.getPassword());
        parameters.put("dbtype", "postgis");
        return parameters;
    }
}

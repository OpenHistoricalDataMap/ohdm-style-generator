package geoserver.cataloginfo.wrapper;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;

public class DataStoreInfoWrapper {

    private Catalog catalog;

    private DataStoreInfo dataStoreInfo;

    public DataStoreInfoWrapper(DataStoreInfo dataStoreInfo, WorkspaceInfo workspaceInfo, String name, String type) {
        this.dataStoreInfo = dataStoreInfo;
        dataStoreInfo.setName(name);
        dataStoreInfo.setWorkspace(workspaceInfo);
        dataStoreInfo.setType(type);
        dataStoreInfo.setEnabled(true);

    }

    public StoreInfo getDataStore() {
        return dataStoreInfo;
    }

}

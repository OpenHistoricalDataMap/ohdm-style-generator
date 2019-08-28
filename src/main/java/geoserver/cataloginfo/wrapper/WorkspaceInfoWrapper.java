package geoserver.cataloginfo.wrapper;

import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;

import java.util.UUID;

public class WorkspaceInfoWrapper {

    private WorkspaceInfo workspaceInfo;

    public WorkspaceInfoWrapper(WorkspaceInfoImpl workspaceInfoImpl, String name) {
        workspaceInfoImpl.setId(generateID());
        workspaceInfoImpl.setName(name);
        workspaceInfo = workspaceInfoImpl;
    }

    private static String generateID() {
        return "WorkspaceInfoImpl--" + UUID.randomUUID().toString();
    }

    public WorkspaceInfo getWorkspace() {
        return workspaceInfo;
    }
}

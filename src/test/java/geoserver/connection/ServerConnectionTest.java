package geoserver.connection;

import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * You have to place a file called geoserver-config.json with valid connection properties placed in src/test/resources/.env
 * in order for this test to work.
 */
class ServerConnectionTest {

    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private File connectionConfigFile = Paths.get(resourceDirectory.toString(), ".env/geoserver-config.json").toFile();
    private File testFile = Paths.get(resourceDirectory.toString(), "testfile.txt").toFile();

    private ServerConnectionConfig serverConnectionConfig;

    @BeforeEach
    void setup() throws FileNotFoundException {
        serverConnectionConfig = ServerConnectionFactory.createConnectionConfigFromJSON(connectionConfigFile);
    }

    @Test
    void testConnection() {
        ServerConnection serverConnection = new ServerConnection();
        assertDoesNotThrow(() -> {
            serverConnection.connect(serverConnectionConfig);
            assertTrue(serverConnection.isConnected());

            serverConnection.disconnect();
            assertFalse(serverConnection.isConnected());
        });
    }

    @Test
    void uploadFileToServer() {
        ServerConnection serverConnection = new ServerConnection();
        assertDoesNotThrow(() -> {
            serverConnection.connect(serverConnectionConfig);
            assertTrue(serverConnection.isConnected());

            // upload a test file
            serverConnection.uploadFileToServer(testFile, serverConnectionConfig.getPath());

            // check if file exists on server
            List<RemoteResourceInfo> remoteResourceInfos = serverConnection.sftpClient.ls(serverConnectionConfig.getPath());
            List<String> fileNames = remoteResourceInfos.stream().map(RemoteResourceInfo::getName).collect(Collectors.toList());
            assertTrue(fileNames.contains(testFile.getName()));

            // delete it
            serverConnection.sftpClient.rm(Paths.get(serverConnectionConfig.getPath(), testFile.getName()).toString());

            serverConnection.disconnect();
        });
    }
}
package geoserver.connection;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.Security;

public class ServerConnection {

    private final Logger logger = LogManager.getLogger(ServerConnection.class);

    SSHClient sshClient;
    SFTPClient sftpClient;

    private boolean unixSystem = true;

    public ServerConnection() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public boolean connect(ServerConnectionConfig connectionConfig) {
        sshClient = new SSHClient();
        if (tryConnectToServer(connectionConfig.getHost())) {
            if (tryAuthentication(connectionConfig.getUser(), connectionConfig.getPassword())) {
                setUnixSystemBasedOnPath(connectionConfig.getPath());
                SFTPClient sftp = tryCreateSFTPClient(sshClient);

                if (sftp != null) {
                    sftpClient = sftp;
                    return true;
                } else {
                    return false;
                }

            }
        }
        return false;
    }

    private void setUnixSystemBasedOnPath(String path) {
        if (path.contains("/")) {
            unixSystem = true;
        } else {
            unixSystem = false;
        }
    }

    public boolean disconnect() {
        return tryCloseSFTPClient(sftpClient) && tryDisconnectSSHClient(sshClient);
    }

    public boolean isConnected() {
        return sshClient != null && sshClient.isConnected();
    }

    public void uploadFileToServer(File localFile, String destinationPath) {
        if (sftpClient != null) {
            tryUploadFileWithSFTPClient(localFile, destinationPath);
        }
    }

    private void tryUploadFileWithSFTPClient(File file, String destinationPath) {
        String hostSystemSpecificPath = getHostSystemSpecificPath(destinationPath);
        try {
            // create parent directories of file if they dont exist
            File destinationFile = new File(hostSystemSpecificPath);
            sftpClient.mkdirs(destinationFile.getParent());

            FileSystemFile source = new FileSystemFile(file.getCanonicalFile());
            sftpClient.put(source, hostSystemSpecificPath);
            logger.info("Successfully uploaded {} to {}.", source.getFile(), hostSystemSpecificPath);
        } catch (IOException e) {
            logger.error("Could not upload file {} to {}. \n\t Cause: {}", file, hostSystemSpecificPath, e.getMessage());
        }
    }

    private boolean tryConnectToServer(String host) {
        try {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(host);
            return true;
        } catch (IOException | NullPointerException e) {
            logger.error("Could not connect to server! \n\t Cause: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryAuthentication(String userName, String password) {
        try {
            sshClient.authPassword(userName, password);
            return true;
        } catch (UserAuthException e) {
            logger.error("User and password rejected!\n\t Cause: {}", e.getMessage());
            return false;
        } catch (TransportException e) {
            logger.error("Connection error! \n\t Cause: {}", e.getMessage());
            return false;
        }
    }

    private SFTPClient tryCreateSFTPClient(SSHClient ssh) {
        try {
            return ssh.newSFTPClient();
        } catch (IOException | NullPointerException e) {
            logger.error("Could not create SFTPClient. \n\t Cause: {}", e.getMessage());
            return null;
        }
    }

    private boolean tryCloseSFTPClient(SFTPClient sftpClient) {
        try {
            sftpClient.close();
            return true;
        } catch (IOException | NullPointerException e) {
            logger.error("Could not close SFTP client. \n\t Cause: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryDisconnectSSHClient(SSHClient sshClient) {
        try {
            sshClient.disconnect();
            return true;
        } catch (IOException | NullPointerException e) {
            logger.error("Could not disconnect SSH client. \n\t Cause: {}", e.getMessage());
            return false;
        }
    }

    private String getHostSystemSpecificPath(String path) {
        if (unixSystem) {
            return FilenameUtils.separatorsToUnix(path);

        } else {
            return FilenameUtils.separatorsToWindows(path);
        }
    }

}

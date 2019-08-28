package logging;

import org.geotools.util.logging.Logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerConfiguration {

    /**
     * Accesses the Logger instances that are used within the geotools and geoserver framework and set their level to SEVERE to prevent output
     * of unnecessary information in STDOUT.
     */
    public static void muteGeoServerLogs() {
        Logger logger;
        logger = Logging.getLogger("org.geoserver.platform");
        logger.setLevel(Level.SEVERE);
        logger = Logging.getLogger("org.geoserver");
        logger.setLevel(Level.SEVERE);
        logger = Logging.getLogger("org.geotools");
        logger.setLevel(Level.SEVERE);
    }
}

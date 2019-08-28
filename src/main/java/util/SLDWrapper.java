package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.styling.SLDTransformer;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SLDWrapper {

    private final Logger logger = LogManager.getLogger(SLDWrapper.class);

    private StyledLayerDescriptor styledLayerDescriptor;

    public SLDWrapper(StyledLayerDescriptor styledLayerDescriptor) {
        this.styledLayerDescriptor = styledLayerDescriptor;
    }

    public File writeFileTo(File targetDirectory) {
        Path pathToFile = Paths.get(targetDirectory.getAbsolutePath(), styledLayerDescriptor.getName() + ".sld");
        File sldFile = new File(pathToFile.toString());

        try (FileOutputStream fos = new FileOutputStream(sldFile)) {
            SLDTransformer tx = new SLDTransformer();
            tx.setIndentation(2);
            tx.transform(styledLayerDescriptor, fos);
            return sldFile;
        } catch (IOException | TransformerException e) {
            logger.error("Something went wrong while writing the SLD to file.", e);
            return null;
        }

    }

    public StyledLayerDescriptor getStyledLayerDescriptor() {
        return styledLayerDescriptor;
    }
}

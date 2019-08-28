package cli;

import db.DataSourceConfig;
import db.DataSourceFactory;
import generation.parser.ConfigParseResult;
import generation.parser.ConfigParser;
import generation.parser.ParseException;
import generation.processing.ParseResultProcessor;
import geoserver.GeoServerConfigurator;
import geoserver.connection.ServerConnectionConfig;
import geoserver.connection.ServerConnectionFactory;
import model.classification.Classification;
import model.classification.ClassificationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.styling.StyledLayerDescriptor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import util.SLDWrapper;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

@Command(description = "Generates styles for the OHDM GeoServer based on a configuration ohdmConfigFile.",
        name = "ohdm-style-generator", mixinStandardHelpOptions = true, version = "ohdm-style-generator 1.0")
public class GeneratorCommand implements Callable<Integer> {
    private final Logger logger = LogManager.getLogger(GeneratorCommand.class);

    static File DEFAULT_OUTPUT_DIRECTORY;

    static {
        try {
            DEFAULT_OUTPUT_DIRECTORY = Files.createTempDirectory("generation").toFile();
        } catch (IOException e) {
            DEFAULT_OUTPUT_DIRECTORY = new File(System.getProperty("user.dir"));
        }
    }


    @Parameters(arity = "1", index = "0", description = "The config file that contains the configuration for the generation of the styles.")
    File ohdmConfigFile = null;

    @Option(names = {"-o", "--output-dir"}, description = "The relative or absolute path to the directory where the output will be stored.")
    File outputDirectory = DEFAULT_OUTPUT_DIRECTORY;

    @Option(names = {"-db", "--db-config"}, description = "The relative or absolute path to a .json file containing the parameters for the database connection.")
    File databaseConfigFile = null;

    @Option(names = {"-c", "--geoserver-config"}, description = "The relative or absolute path to a .json file containing the parameters for the connection to the GeoServer server.")
    File connectionConfigFile = null;

    @Option(names = {"-d", "--generate-defaults"}, description = "Defines whether defaults should be generated for MapFeatures that are not declared.")
    boolean generateDefaults = false;

    @Override
    public Integer call() throws Exception {
        assertArguments();
        // create directory if it doesn't exist
        if (!Files.exists(Paths.get(outputDirectory.getAbsolutePath()))) {
            logger.info("Creating directory {}", outputDirectory.getAbsolutePath());
            Files.createDirectories(outputDirectory.toPath());
        }

        Classification classification = getClassification();


        Collection<StyledLayerDescriptor> styledLayerDescriptors = getSLDs(classification);
        if (styledLayerDescriptors == null) {
            // return with non-zero exit code
            return 1;
        }

        Collection<File> sldFiles = writeSLDsToFiles(styledLayerDescriptors, outputDirectory);

        if (connectionConfigFile != null) {
            configureGeoServer(classification, sldFiles);
        }

        return 0;
    }

    private void assertArguments() throws FileNotFoundException {
        if (!Files.exists(Paths.get(ohdmConfigFile.getAbsolutePath()))) {
            logger.error("The provided configuration does not exist: {}", ohdmConfigFile.getAbsolutePath());
            throw new FileNotFoundException("File not found!");
        }

        if (connectionConfigFile != null && !Files.exists(Paths.get(connectionConfigFile.getAbsolutePath()))) {
            logger.error("The provided connection configuration does not exist: {}", connectionConfigFile.getAbsolutePath());
            throw new FileNotFoundException("File not found!");
        }
        if (databaseConfigFile != null && !Files.exists(Paths.get(databaseConfigFile.getAbsolutePath()))) {
            logger.error("The provided database configuration does not exist: {}", connectionConfigFile.getAbsolutePath());
            throw new FileNotFoundException("File not found!");
        }
    }

    private Collection<File> writeSLDsToFiles(Collection<StyledLayerDescriptor> styledLayerDescriptors, File targetDirectory) {
        Collection<File> sldFiles = new ArrayList<>();

        styledLayerDescriptors.forEach(sld -> {
            SLDWrapper sldWrapper = new SLDWrapper(sld);
            File file = sldWrapper.writeFileTo(targetDirectory);
            logger.info("Writing SLD {} to file {}...", sld.getName(), file.getPath());
            sldFiles.add(file);
        });

        return sldFiles;
    }

    private Classification getClassification() {
        Classification classification;
        if (databaseConfigFile != null) {
            try {
                DataSourceConfig dataSourceConfig = DataSourceFactory.createDataSourceConfigFromJSON(databaseConfigFile);
                DataSource dataSource = DataSourceFactory.createDataSourceFrom(dataSourceConfig);
                classification = ClassificationFactory.getClassificationFromDataSource(dataSource);
            } catch (SQLException | FileNotFoundException ex) {
                logger.error("Something went wrong while retrieving the classification from the defined datasource. \n {} \n Using the default classification instead.", ex.getMessage());
                classification = ClassificationFactory.getDefaultClassification();
            }
        } else {
            logger.info("No classification defined. Using the default classification instead.");
            classification = ClassificationFactory.getDefaultClassification();
        }

        return classification;
    }

    private Collection<StyledLayerDescriptor> getSLDs(Classification classification) {
        try {
            ConfigParseResult configParseResult = ConfigParser.parse(ohdmConfigFile);
            ParseResultProcessor parseResultProcessor = new ParseResultProcessor(classification);
            return parseResultProcessor.getStyledLayerDescriptorsFromParseResult(configParseResult, generateDefaults);
        } catch (ParseException | IOException e) {
            logger.error("Something went wrong while parsing the OHDM configuration file. \n\t Cause: {}", e.getMessage());
            return null;
        }
    }

    private void configureGeoServer(Classification classification, Collection<File> sldFiles) throws FileNotFoundException {
        ServerConnectionConfig serverConnectionConfig = ServerConnectionFactory.createConnectionConfigFromJSON(connectionConfigFile);

        GeoServerConfigurator configurator = GeoServerConfigurator.getInstance(serverConnectionConfig);
        try {
            logger.info("Uploading generated SLDs to GeoServer...");
            configurator.uploadSLDsToGeoServer(sldFiles);

            if (databaseConfigFile != null) {
                logger.info("Starting configuration of GeoServer with database config...");
                DataSourceConfig dataSourceConfig = DataSourceFactory.createDataSourceConfigFromJSON(databaseConfigFile);
                configurator.configureGeoServerWithDataSource(classification, sldFiles, dataSourceConfig);
            } else {
                logger.info("Starting configuration of GeoServer without database config...");
                configurator.configureGeoServer(sldFiles);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.error("Could not configure GeoServer. \n\t Cause: {}", e.getMessage());
        }

    }
}
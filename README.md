# ohdm-style-generator

This project aims to simplify the generation of styling configuration files for map renderers.
The general idea is to have one single configuration file that specifies the styling information which is then parsed and processed by this application so that eventually SLD-files are created.
Furthermore it is possible to automate the configuration of a GeoServer with those generated styles by providing additional information about the GeoServer and a database.

## Quickstart with IntelliJ IDEA
1. Clone the repository
2. Open IntelliJ IDEA and close open projects (if any)
3. Choose "Import Project" from the main menu
4. Select the cloned directory and choose "Import project from external model" and select "Gradle" in the list 

## Building
Open a terminal in the root of the project directory and type: 
```
$ ./gradlew shadowJar
```

The generated artifact is called "ohdm-style-generator-1.0-all.jar" and located in the `build/libs` directory.

## Usage
You can either run the application in your IDE or call the generated artifact with your desired parameters.

#### Configuring program arguments in IntelliJ IDEA
You can configure the program arguments for the application in "Run/Debug Configurations".
Add a new configuration for the "Application" type, choose "Main" as main class and select "ohdm-style-generator.main" as classpath of module.
Afterwards you can add the desired arguments to the "Program arguments" field.


## Running the tests
In order to run the tests you have to create a directory called `.env` in the `src/test/resources/` directory and place two files named `datasource-config.json` and `geoserver-config.json` in it.
These two files need to contain valid parameters.

#### Sample datasource-config.json
```
{
  "host": "myHost",
  "database": "myDatabase",
  "port": 5432,
  "user": "myUser",
  "password": "myPassword",
  "schema": "mySchema"
}
```
#### Sample geoserver-config.json
```
{
  "host": "myHost",
  "user": "myUser",
  "password": "myPassword",

  "path": "/path/to/my/geoserver/data_dir",
  "workspaceName": "myWorkspaceName",
  "namespaceName": "myNamespaceName",
  "storeName": "myStoreName",
  "crsCode": "EPSG:3857"
}
```

## About
This project was build as part of a bachelor thesis.
The bachelor thesis is written in German language and included in this repository (`bachelor-thesis.pdf`). 
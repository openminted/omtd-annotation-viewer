# OMTD Annotation Viewer

The OMTD Annotation Viewer is a [Google Web Toolkit (GWT)](http://www.gwtproject.org) web application.  It requires a REST web service API to be available, to retrieve the annotated documents, and this project provides an implementation of such a service using [Spring Boot](https://projects.spring.io/spring-boot/).

# Configuring the Spring Boot REST web service

The Spring Boot web service, providing the annotated documents, has two modes of operation: production and demonstration.  

If the Spring Boot configuration parameter `viewer.omtd.store.url` is defined (which should specify the URL to a running OMTD Store instance, e.g. `http://store.openminted.eu:9000`) then the web service will run in production mode.  This mode will attempt to load annotated documents from the OMTD store instance.

Demonstration mode requires no configuration and, no matter what document is requested by the viewer, the web service will always return the same test document.

## Production mode

Production mode requires the definition of a single configuration parameter called `viewer.omtd.store.url`.  Spring Boot can be configured in a number ways, so please see the [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) section in the Spring Boot documentation for more information on how to do this.

One way to set configuration parameter values is using a `application.properties` file, which is either located in a position automatically checked by Spring Boot or whose location is passed as a command line argument to the Spring Boot instance on startup.  The file contents, for the Annotation Viewer, will look similiar to this:

```properties
viewer.omtd.store.url=http://store.openminted.eu:9000
```

When building the Annotation Viewer from the source code, especially during development, the simplest way of setting this configuration parameter is to modify the `[applications.properties](https://github.com/openminted/omtd-annotation-viewer/blob/master/omtd-annotation-viewer-server/src/main/resources/application.properties)` file in the `omtd-annotation-viewer-server` project.

## Demonstration mode

To make the Spring Boot REST web service run in demonstration mode, please ensure that the configuration parameter `viewer.omtd.store.url` is not defined within any configuration file loaded by the Spring Boot instance (please see the [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) section in the Spring Boot documentation for more information).


## Running OMTD Annotation Viewer during development

When developing the Annotation Viewer it is recommended to use GWT's [Super Dev Mode](http://www.gwtproject.org/articles/superdevmode.html) to provide a faster and more productive way to view changes in the GWT code.  Super Dev Mode is implemented using its own code server, which is responsible for watching for changes in the source and then performing incremental compilation upon a page refresh in the web browser.

To use Super Dev Mode during development, both the Super Dev Mode code server and the Spring Boot server are required to be running at the same time.  This is achieved using two calls with Maven (shown below in command line format, however these can be ran using Launch Configurations within the Eclipse IDE).

```bash
# Starts GWT Super Dev Mode code server
cd /path/to/omtd-annotation-viewer
mvn gwt:codeserver
```

```bash
# Starts Spring Boot web service
cd /path/to/omtd-annotation-viewer/omtd-annotation-viewer-server
mvn spring-boot:run -P development
```


## Using OMTD Annotation Viewer

http://localhost:8080/#/?archiveId=123&documentId=PMC4689373.pdf


## Deploying OMTD Annotation Viewer

``` bash
cd /path/to/omtd-annotation-viewer
mvn install
```


## Source code overview



## Spring Boot-powered REST web service API


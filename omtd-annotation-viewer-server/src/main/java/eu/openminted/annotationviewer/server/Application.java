package eu.openminted.annotationviewer.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UIMAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;

import eu.openminted.annotationviewer.server.service.AnnotationService;
import eu.openminted.annotationviewer.server.service.impl.DemoAnnotationService;
import eu.openminted.annotationviewer.server.service.impl.OmtdAnnotationService;

@SpringBootApplication
public class Application {
	
	private final Logger log = LoggerFactory.getLogger(Application.class);

	// If this value is not set, the application will enter demo mode.
	@Value("${viewer.omtd.store.url:#{null}}")
	private URL omtdStoreUrl;

	@Bean
	AnnotationService annotationService() throws MalformedURLException {
		if (omtdStoreUrl != null) {
			log.info("running in Production Mode");
			return new OmtdAnnotationService(omtdStoreUrl);
		} else {
			log.info("running in Demo Mode");
			return new DemoAnnotationService();
		}
	}

	public static void main(String[] args) throws IOException, UIMAException, SAXException {
		SpringApplication.run(Application.class, args);
	}
}

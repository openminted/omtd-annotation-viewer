package eu.openminted.annotationviewer.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UIMAException;
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

	// If this value is not set, the application will enter demo mode.
	@Value("${viewer.omtd.store.url:#{null}}")
	private URL omtdStoreUrl;

	@Bean
	AnnotationService annotationService() throws MalformedURLException {
		if (omtdStoreUrl != null) {
			return new OmtdAnnotationService(omtdStoreUrl);
		} else {
			// TODO demo service if set
			return new DemoAnnotationService();
		}
	}

	public static void main(String[] args) throws IOException, UIMAException, SAXException {
		SpringApplication.run(Application.class, args);
	}
}

package eu.openminted.annotationviewer.server.web;

import java.io.OutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import eu.openminted.annotationviewer.server.service.AnnotationService;

@RestController
public class AnnotationController {

	private final AnnotationService annotationService;

	public AnnotationController(AnnotationService annotationService) {
		this.annotationService = annotationService;
	}

	@GetMapping("/document/{archiveId}/{documentId:.+}")
	void getDocument(@PathVariable String archiveId, @PathVariable String documentId, OutputStream os)
			throws Exception {
		annotationService.writeDocumentJson(archiveId, documentId, os);
	}
}

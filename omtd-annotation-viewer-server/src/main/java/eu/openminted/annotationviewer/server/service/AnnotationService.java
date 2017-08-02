package eu.openminted.annotationviewer.server.service;

import java.io.OutputStream;

public interface AnnotationService {
	void writeDocumentJson(String archiveId, String documentId, OutputStream os) throws Exception;

}

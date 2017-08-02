package eu.openminted.annotationviewer.client.service;

import eu.openminted.annotationviewer.client.service.dto.Document;
import rx.Single;

public interface AnnotationRestApi {
	public Single<Document> getDocument(String archiveId, String documentId);
		
}

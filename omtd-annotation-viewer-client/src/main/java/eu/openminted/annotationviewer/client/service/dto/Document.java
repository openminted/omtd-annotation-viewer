package eu.openminted.annotationviewer.client.service.dto;

import eu.openminted.annotationviewer.client.uima.Cas;

public class Document {
	private final Cas cas;

	public Document(Cas cas) {
		this.cas = cas;
	}

	public Cas getCas() {
		return cas;
	}
}
package eu.openminted.annotationviewer.server.service;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import eu.openminted.uima.jsog.JsogCasSerialiser;

public class AnnotationServiceUtils {

	private static final String TYPE_SYSTEM_XML_FILENAME = "typesystem.xml";
	private static final String DOCUMENTS_FOLDER_FILENAME = "documents";

	private static final JsonFactory JSON_FACTORY = new JsonFactory();
	private static final JsogCasSerialiser jsogCasSerialiser = new JsogCasSerialiser();

	public static String getTypeSystemPath(String archiveId) {
		return archiveId + '/' + TYPE_SYSTEM_XML_FILENAME;
	}

	public static String getDocumentPath(String archiveId, String documentId) {
		return archiveId + '/' + DOCUMENTS_FOLDER_FILENAME + '/' + documentId;
	}

	public static final void writeDocumentJson(CAS cas, OutputStream os)
			throws IOException, InvalidXMLException, ResourceInitializationException, SAXException {
		JsonGenerator json = JSON_FACTORY.createGenerator(os);
		json.setPrettyPrinter(new DefaultPrettyPrinter());
		json.writeStartObject();
		json.writeFieldName("cas");
		jsogCasSerialiser.serialise(cas, json);
		json.writeEndObject();
		json.close();
	}
}

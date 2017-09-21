package eu.openminted.annotationviewer.server.service.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

import eu.openminted.annotationviewer.server.service.AnnotationService;
import eu.openminted.annotationviewer.server.service.AnnotationServiceUtils;
import eu.openminted.store.common.StoreResponse;

public class DemoAnnotationService implements AnnotationService {

	private final static String ARCHIVE_PATH_PREFIX = "/eu/openminted/annotationviewer/server/demo/";

	@Override
	public void writeDocumentJson(String archiveId, String documentId, OutputStream os) throws Exception {
		archiveId = ARCHIVE_PATH_PREFIX + "Archive1";
		documentId = "Document1.xmi";
		TypeSystemDescription tsd = loadTypeSystem(archiveId);
		addTestTypes(tsd);
		CAS cas = loadCas(archiveId, documentId, tsd);
		addTestView(cas);
		AnnotationServiceUtils.writeDocumentJson(cas, os);
	}

	private CAS loadCas(String archiveId, String documentId, TypeSystemDescription tsd)
			throws ResourceInitializationException, InvalidXMLException, SAXException, IOException {
		String documentPath = AnnotationServiceUtils.getDocumentPath(archiveId, documentId);
		InputStream documentIs = getClass().getResourceAsStream(documentPath);
		CAS cas = CasCreationUtils.createCas(tsd, null, null);
		XmiCasDeserializer.deserialize(documentIs, cas);
		return cas;
	}

	private TypeSystemDescription loadTypeSystem(String archiveId) throws InvalidXMLException {
		String typeSystemPath = AnnotationServiceUtils.getTypeSystemPath(archiveId);
		InputStream typeSystemIs = getClass().getResourceAsStream(typeSystemPath);
		XMLInputSource xmlIs = new XMLInputSource(typeSystemIs, null);
		return UIMAFramework.getXMLParser().parseTypeSystemDescription(xmlIs);
	}

	private void addTestTypes(TypeSystemDescription tsd) {
		TypeDescription entityType = tsd.addType("uk.ac.nactem.Entity", "", CAS.TYPE_NAME_ANNOTATION);
		TypeDescription eventType = tsd.addType("uk.ac.nactem.Event", "", CAS.TYPE_NAME_ANNOTATION);
		eventType.addFeature("arguments", "", CAS.TYPE_NAME_FS_ARRAY);
		TypeDescription arrayType = tsd.addType("uk.ac.nactem.ArrayTest", "", CAS.TYPE_NAME_ANNOTATION);
		arrayType.addFeature("booleanArray", "", CAS.TYPE_NAME_BOOLEAN_ARRAY);
		arrayType.addFeature("integerArray", "", CAS.TYPE_NAME_INTEGER_ARRAY);
		arrayType.addFeature("stringArray", "", CAS.TYPE_NAME_STRING_ARRAY);
		arrayType.addFeature("annotationArray", "", CAS.TYPE_NAME_FS_ARRAY);

	}

	private AnnotationFS newAnnotation(CAS cas, Type type, int begin, int end) {
		AnnotationFS ann = cas.createAnnotation(type, begin, end);
		cas.addFsToIndexes(ann);
		return ann;
	}
	
	private void addTestView(CAS cas) throws IOException {
		String text = IOUtils.toString(getClass().getResourceAsStream(ARCHIVE_PATH_PREFIX + "Chapter1.txt"));

		CAS cas2 = cas.createView("Alice in Wonderland");
		cas2.setDocumentText(text);
		
		Type entityType = cas.getTypeSystem().getType("uk.ac.nactem.Entity");
		Type eventType = cas.getTypeSystem().getType("uk.ac.nactem.Event");
		Feature eventArgumentsFeature = eventType.getFeatureByBaseName("arguments");

		Type type = cas.getTypeSystem().getType("uk.ac.nactem.ArrayTest");
		Feature bArrayFeature = type.getFeatureByBaseName("booleanArray");
		Feature iArrayFeature = type.getFeatureByBaseName("integerArray");
		Feature fsArrayFeature = type.getFeatureByBaseName("annotationArray");
		Feature sArrayFeature = type.getFeatureByBaseName("stringArray");

		AnnotationFS alice = newAnnotation(cas2, entityType, 33, 38);
		AnnotationFS sister = newAnnotation(cas2, entityType, 89, 96);
		newAnnotation(cas2, entityType, 185, 191);
		newAnnotation(cas2, entityType, 293, 298);
		AnnotationFS rabbit = newAnnotation(cas2, entityType, 581, 593);
		AnnotationFS her = newAnnotation(cas2, entityType, 622, 625);
		
		AnnotationFS event = newAnnotation(cas2, eventType, 581, 625);
		ArrayFS argsArray = cas2.createArrayFS(2);
		argsArray.set(0, rabbit);
		argsArray.set(1, her);
		event.setFeatureValue(eventArgumentsFeature, argsArray);

		BooleanArrayFS bArray = cas2.createBooleanArrayFS(3);
		bArray.set(0, true);
		bArray.set(1, false);

		IntArrayFS iArray = cas2.createIntArrayFS(5);
		iArray.set(0, -2);
		iArray.set(2, 3);
		iArray.set(4, 12345);

		ArrayFS fsArray = cas2.createArrayFS(3);
		fsArray.set(0, alice);
		fsArray.set(1, sister);
		fsArray.set(2, event);

		StringArrayFS sArray = cas2.createStringArrayFS(5);
		sArray.set(0, "Some");
		sArray.set(1, "string");
		sArray.set(2, "values");
		sArray.set(3, "to");
		sArray.set(4, "test");

		AnnotationFS fs = cas2.createAnnotation(type, 0, 31);
		fs.setFeatureValue(bArrayFeature, bArray);
		fs.setFeatureValue(iArrayFeature, iArray);
		fs.setFeatureValue(fsArrayFeature, fsArray);
		fs.setFeatureValue(sArrayFeature, sArray);
		cas2.addFsToIndexes(fs);
	}

	public static void main(String[] args) throws Exception {
		final String archiveId = ARCHIVE_PATH_PREFIX + "Archive1";
		final String documentId = "Document1.xmi";

		DemoAnnotationService service = new DemoAnnotationService();
		TypeSystemDescription tsd = service.loadTypeSystem(archiveId);
		service.addTestTypes(tsd);
		CAS cas = service.loadCas(archiveId, documentId, tsd);
		service.addTestView(cas);
		//XmiCasSerializer.serialize(cas, new FileOutputStream("document2.xmi"));

	}

}

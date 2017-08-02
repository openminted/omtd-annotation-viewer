package eu.openminted.annotationviewer.server.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

import eu.openminted.annotationviewer.server.service.AnnotationService;
import eu.openminted.annotationviewer.server.service.AnnotationServiceUtils;
import eu.openminted.store.common.StoreResponse;
import eu.openminted.store.restclient.StoreRESTClient;

public class OmtdAnnotationService implements AnnotationService {

	private final StoreRESTClient omtdStore;

	public OmtdAnnotationService(URL storeUrl) {
		omtdStore = new StoreRESTClient(storeUrl.toString());
	}

	@Override
	public void writeDocumentJson(String archiveId, String documentId, OutputStream os) throws Exception {
		CAS cas = loadXmiCas(archiveId, documentId);
		AnnotationServiceUtils.writeDocumentJson(cas, os);
	}

	protected CAS loadXmiCas(String archiveId, String documentId)
			throws IOException, InvalidXMLException, ResourceInitializationException, SAXException {

		String documentPath = AnnotationServiceUtils.getDocumentPath(archiveId, documentId);
		Path outputPath = Files.createTempFile(documentId, ".xmi");
		StoreResponse response = omtdStore.downloadFile(documentPath, outputPath.toString());
		TypeSystemDescription tsd = loadXmlTypeSystem(archiveId);

		CAS cas = CasCreationUtils.createCas(tsd, null, null);
		XmiCasDeserializer.deserialize(new FileInputStream(outputPath.toFile()), cas);	

		return cas;
	}

	// TODO will it be a single type system file per archive or per document?
	TypeSystemDescription loadXmlTypeSystem(String archiveId) throws InvalidXMLException, IOException {
		String typeSystemPath = AnnotationServiceUtils.getTypeSystemPath(archiveId);
		Path outputPath = Files.createTempFile(archiveId, ".ts.xml");
		StoreResponse response = omtdStore.downloadFile(typeSystemPath, outputPath.toString());
		return UIMAFramework.getXMLParser().parseTypeSystemDescription(new XMLInputSource(outputPath.toFile()));
	}

}

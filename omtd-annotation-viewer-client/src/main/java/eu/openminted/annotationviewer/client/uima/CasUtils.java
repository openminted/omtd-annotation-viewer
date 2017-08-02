package eu.openminted.annotationviewer.client.uima;

import java.util.Iterator;

import com.google.common.base.Strings;

public class CasUtils {
	private final static String TYPE_NAME_DKPRO_DOCUMENT_META_DATA = "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData";
	private final static String FEATURE_NAME_DOCUMENT_TITLE = "documentTitle";
	private final static String FEATURE_NAME_DOCUMENT_ID = "documentId";
	
	public static String getTitle(Cas cas) {
		Iterator<Annotation> it = cas.getAllAnnotations(TYPE_NAME_DKPRO_DOCUMENT_META_DATA);
		if (it.hasNext()) {
			Annotation documentMetaData = it.next();
			String title = documentMetaData.getFeatureValueAsString(FEATURE_NAME_DOCUMENT_TITLE);
			if (!Strings.isNullOrEmpty(title)) {
				return title;
			}
			title = documentMetaData.getFeatureValueAsString(FEATURE_NAME_DOCUMENT_ID);
			if (!Strings.isNullOrEmpty(title)) {
				return title;
			}
		}
		
		return null;
	}
}

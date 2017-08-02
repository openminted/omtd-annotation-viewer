package eu.openminted.annotationviewer.client.uima;

import java.util.Iterator;

public interface Cas {

	static final String NAME_DEFAULT_SOFA = "_InitialView";
	
	static final String FEATURE_BASE_NAME_BEGIN = "begin";

	static final String FEATURE_BASE_NAME_END = "end";

	static final String FEATURE_BASE_NAME_SOFAID = "sofaID";

	static final String FEATURE_BASE_NAME_SOFAMIME = "mimeType";

	static final String FEATURE_BASE_NAME_SOFANUM = "sofaNum";

	static final String FEATURE_BASE_NAME_SOFAURI = "sofaURI";

	static final String FEATURE_BASE_NAME_SOFASTRING = "sofaString";

	static final String UIMA_CAS_PREFIX = "uima.cas.";

	static final String TYPE_NAME_SOFA = UIMA_CAS_PREFIX + "Sofa";
	
	  static final String TYPE_NAME_FS_ARRAY = UIMA_CAS_PREFIX + "FSArray";

	  static final String TYPE_NAME_INTEGER_ARRAY = UIMA_CAS_PREFIX + "IntegerArray";

	  static final String TYPE_NAME_FLOAT_ARRAY = UIMA_CAS_PREFIX + "FloatArray";


	  static final String TYPE_NAME_STRING_ARRAY = UIMA_CAS_PREFIX + "StringArray";

	  static final String TYPE_NAME_BOOLEAN_ARRAY = UIMA_CAS_PREFIX + "BooleanArray";

	  static final String TYPE_NAME_BYTE_ARRAY = UIMA_CAS_PREFIX + "ByteArray";


	  static final String TYPE_NAME_SHORT_ARRAY = UIMA_CAS_PREFIX + "ShortArray";


	  static final String TYPE_NAME_LONG_ARRAY = UIMA_CAS_PREFIX + "LongArray";

	  static final String TYPE_NAME_DOUBLE_ARRAY = UIMA_CAS_PREFIX + "DoubleArray";

	Iterator<Annotation> getIndexedAnnotations();
	
	Annotation getAnnotation(int id);
	Iterator<Annotation> getAllAnnotations();
	Iterator<Annotation> getAllAnnotations(String typeName);

	void addAnnotationToIndex(Annotation annotation);

	Cas createView(String name);

	Cas getView(String name);

	String getViewName();

	Iterator<Cas> getViewIterator();

	Sofa getSofa();
	
	String getDocumentText();

}

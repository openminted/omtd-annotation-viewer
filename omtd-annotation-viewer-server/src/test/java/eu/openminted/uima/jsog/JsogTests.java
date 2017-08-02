package eu.openminted.uima.jsog;

import java.io.IOException;
import java.util.Arrays;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.Annotation_Type;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JsogTests {

	private static final String TEST_TYPE_SYSTEM = "eu.openminted.uima.jsog.TypeSystem";
	private static final String TEST_TYPE_NAME = "eu.openminted.uima.jsog.test.Type";

	@Test
	public void jsogTest() throws ResourceInitializationException, InvalidXMLException, IOException, SAXException {
		TypeSystemDescription ts = TypeSystemDescriptionFactory.createTypeSystemDescription(TEST_TYPE_SYSTEM);
		ts.resolveImports();

		CAS cas = CasCreationUtils.createCas(ts, null, null);

		
		Type testType = CasUtil.getType(cas, TEST_TYPE_NAME);
		Type annType = cas.getAnnotationType();
		cas.setDocumentText("This is some random text");
		AnnotationFS ann = cas.createAnnotation(testType, 1, 2);
		cas.addFsToIndexes(ann);
		FSUtil.setFeature(ann, "booleanArray", Arrays.asList(true, false));
		FSUtil.setFeature(ann, "doubleArray", Arrays.asList(Double.MAX_VALUE, Double.MIN_VALUE));
		FSUtil.setFeature(ann, "floatArray", Arrays.asList(Float.MAX_VALUE, Float.MIN_VALUE));
		FSUtil.setFeature(ann, "intArray", Arrays.asList(Integer.MAX_VALUE, Integer.MIN_VALUE));
		FSUtil.setFeature(ann, "longArray", Arrays.asList(Long.MAX_VALUE, Long.MIN_VALUE));
		FSUtil.setFeature(ann, "shortArray", Arrays.asList(Short.MAX_VALUE, Short.MIN_VALUE));
		FSUtil.setFeature(ann, "stringArray", Arrays.asList("hello", "goodbye"));
		
		AnnotationFS ann2 = cas.createAnnotation(annType, 3, 6);
		AnnotationFS ann3 = cas.createAnnotation(annType, 7, 8);
		FSUtil.setFeature(ann, "fsArray", Arrays.asList(ann2, ann3));
		cas.addFsToIndexes(ann2);
		
		//JsogCasSerialiser.serialise(cas, System.out);
	}
}

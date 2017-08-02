package eu.openminted.annotationviewer.client.uima;

import java.util.Iterator;

public interface Annotation {

	int getId();
	
	Cas getCas();

	String getTypeName();

	boolean hasFeatureValue(String featureName);

	Iterator<String> getFeatureNames();

	int getFeatureValueAsInt(String featureName);

	String getFeatureValueAsString(String featureName);

	Annotation getFeatureValueAsAnnotation(String featureName);

	boolean isAnnotation(String featureName);
	
	boolean isDocumentLevel();
	
	boolean isArray();

	boolean isNull(String featureName);
	
	// For text span annotations
	
	int getBegin();
	
	int getEnd();

	public String getCoveredText();
	
	// For array annotations
	
	int arraySize();
	
	String getArrayValueAsString(int index);
	
	Annotation getArrayValueAsAnnotation(int index);
	

}
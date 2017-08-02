package eu.openminted.annotationviewer.client.uima.impl;

import com.google.gwt.json.client.JSONObject;

import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.uima.Sofa;

public class SofaImpl extends AnnotationImpl implements Sofa {
	

	public SofaImpl(JSONObject value, CasImpl cas) {
		super(value, cas);
	}

	@Override
	public String getSofaID() {
		return getFeatureValueAsString(Cas.FEATURE_BASE_NAME_SOFAID);
	}

	@Override
	public String getSofaURI() {
		return getFeatureValueAsString(Cas.FEATURE_BASE_NAME_SOFAURI);
	}

	@Override
	public String getLocalStringData() {
		return getFeatureValueAsString(Cas.FEATURE_BASE_NAME_SOFASTRING);
	}

}

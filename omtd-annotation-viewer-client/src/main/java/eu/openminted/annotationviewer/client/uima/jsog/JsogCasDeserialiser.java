package eu.openminted.annotationviewer.client.uima.jsog;

import java.util.Iterator;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.uima.Sofa;
import eu.openminted.annotationviewer.client.uima.impl.CasImpl;
import eu.openminted.annotationviewer.client.uima.impl.SofaImpl;

public class JsogCasDeserialiser {

	private static class Data {
		final CasImpl cas = new CasImpl();
		//final Map<Integer, Annotation> annotations = new HashMap<>();
	}

	public Cas deserialise(JSONArray jsonCas) {

		// NOTE JSOG IDs should be strings, but using integers for performance
		// reasons

		Data data = new Data();

		for (int i = 0; i < jsonCas.size(); i++) {
			JSONObject jsonView = jsonCas.get(i).isObject();
			if (jsonView == null) {
				throw new JSONException("Expect JSON array (CAS) items to be JSON objects (CAS Views)");
			}

			parseView(jsonView, data);
		}

		return data.cas;
	}

	private Cas parseView(JSONObject jsonView, Data data) {
		// Parse Sofa
		JSONValue value = jsonView.get("sofa");
		if (value == null) {
			throw new JSONException("Expect JSON Object (CAS View) to contain value 'sofa'");
		}
		JSONObject jsonSofa = value.isObject();
		if (jsonSofa == null) {
			throw new JSONException("Expect JSON value (Sofa) to be a JSON object");
		}

		CasImpl view = parseSofa(jsonSofa, data);

		// Parse annotations
		value = jsonView.get("index");
		if (value == null) {
			throw new JSONException("Expect JSON Object (CAS View) to contain value 'index'");
		}
		JSONArray jsonIndex = value.isArray();
		if (jsonIndex == null) {
			throw new JSONException("Expect JSON value (Annotation Index) to be a JSON Array");
		}
		parseAnnotationIndex(jsonIndex, view, data);

		return view;
	}

	private CasImpl parseSofa(JSONObject jsonSofa, Data data) {
		JSONValue value = jsonSofa.get(Cas.FEATURE_BASE_NAME_SOFAID);
		if (value == null) {
			throw new JSONException("Expect JSON Object (Sofa) to contain value 'sofaID'");
		}
		JSONString jsonSofaId = value.isString();
		if (jsonSofaId == null) {
			throw new JSONException("Expect JSON value (Sofa ID) to be a JSON String");
		}
		String sofaId = jsonSofaId.stringValue();

		final CasImpl cas;
		if (Cas.NAME_DEFAULT_SOFA.equals(sofaId)) {
			cas = data.cas;
		} else if (null == data.cas.getView(sofaId)) {
			cas = data.cas.createView(sofaId);
		} else {
			throw new JSONException("Sofa with id '" + sofaId + "' has already been declared");
		}

		Sofa sofa = new SofaImpl(jsonSofa, cas);
		cas.setSofa(sofa);

		return cas;
	}

	private void parseAnnotationIndex(JSONArray jsonIndex, CasImpl view, Data data) {
		for (int i = 0; i < jsonIndex.size(); i++) {
			JSONObject jsonAnnotation = jsonIndex.get(i).isObject();
			if (jsonAnnotation == null) {
				throw new JSONException("Expect JSON Array items (Annotation Index) to be JSON objects (Annotations");
			}

			Annotation annotation = parseAnnotation(jsonAnnotation, view);

			view.addAnnotationToIndex(annotation);
		}
	}
	
	private Annotation parseAnnotation(JSONObject jsonAnnotation, CasImpl view) {
		final Annotation annotation;
		if (jsonAnnotation.containsKey(JsogConstants.PROPERTY_REF)) {
			annotation = parseAnnotationReference(jsonAnnotation, view);
		} else {
			// Dealing with a new annotation definition.
			// Parse this annotation (and any sub annotations)
			annotation = parseAnnotationDefinition(jsonAnnotation, view);
		}
		return annotation;
	}

	private Annotation parseAnnotationDefinition(JSONObject jsonAnnotation, CasImpl view) {
		Annotation annotation = view.createAnnotation(jsonAnnotation);

		if (Cas.TYPE_NAME_FS_ARRAY.equals(annotation.getTypeName())) {
			JSONArray jsonArray = jsonAnnotation.get("values").isArray();
			if (jsonArray != null) {
				for (int i = 0; i < annotation.arraySize(); i++) {
					JSONValue val = jsonArray.get(i);
					if (val.isNull() != null) {
						continue;
					}

					JSONObject obj = val.isObject();
					if (obj == null) {
						// TODO error
					}

					parseAnnotation(obj, view);
				}
			}
		} else if (!annotation.isArray()) {
			Iterator<String> featureNames = annotation.getFeatureNames();
			while (featureNames.hasNext()) {
				String featureName = featureNames.next();
				if (annotation.isAnnotation(featureName)) {
					JSONObject childAnnotation = jsonAnnotation.get(featureName).isObject();
					parseAnnotation(childAnnotation, view);
				}
			}
		}

		return annotation;
	}

	private Annotation parseAnnotationReference(JSONObject jsonAnnotationReference, CasImpl view) {
		// TODO test we have a valid int value
		JSONNumber id = jsonAnnotationReference.get(JsogConstants.PROPERTY_REF).isNumber();

		//Annotation annotation = data.annotations.get((int) id.doubleValue());
		Annotation annotation = view.getAnnotation((int) id.doubleValue());
		if (annotation == null) {
			throw new JSONException("Object with id '" + id + "' has been referenced but not defined");
		}
		return annotation;
	}
}

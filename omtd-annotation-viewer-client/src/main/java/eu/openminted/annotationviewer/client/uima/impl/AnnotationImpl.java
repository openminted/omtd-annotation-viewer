package eu.openminted.annotationviewer.client.uima.impl;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.uima.jsog.JsogConstants;

public class AnnotationImpl implements Annotation {

	private final CasImpl cas;
	private final JSONObject value;
	private int begin = -1;
	private int end = -1;

	Set<String> arrayTypes = Sets.newHashSet(Cas.TYPE_NAME_FS_ARRAY, Cas.TYPE_NAME_INTEGER_ARRAY,
			Cas.TYPE_NAME_FLOAT_ARRAY, Cas.TYPE_NAME_STRING_ARRAY, Cas.TYPE_NAME_BOOLEAN_ARRAY,
			Cas.TYPE_NAME_BYTE_ARRAY, Cas.TYPE_NAME_SHORT_ARRAY, Cas.TYPE_NAME_SHORT_ARRAY, Cas.TYPE_NAME_LONG_ARRAY,
			Cas.TYPE_NAME_DOUBLE_ARRAY);

	AnnotationImpl(JSONObject value, CasImpl cas) {
		this.value = value;
		this.cas = cas;

		JSONValue beginValue = value.get(Cas.FEATURE_BASE_NAME_BEGIN);
		JSONValue endValue = value.get(Cas.FEATURE_BASE_NAME_END);
		if (beginValue != null && endValue != null) {
			JSONNumber b = beginValue.isNumber();
			JSONNumber e = endValue.isNumber();

			if (b != null && e != null) {
				begin = (int) b.doubleValue();
				end = (int) e.doubleValue();
			}
		}
	}

	@Override
	public Cas getCas() {
		return cas;
	}

	@Override
	public int getId() {
		return (int) value.get(JsogConstants.PROPERTY_ID).isNumber().doubleValue();
	}

	@Override
	public String getTypeName() {
		return value.get(JsogConstants.PROPERTY_TYPE).isString().stringValue();
	}

	@Override
	public int getBegin() {
		return begin;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public boolean hasFeatureValue(String featureName) {
		return value.containsKey(featureName);
	}

	@Override
	public Iterator<String> getFeatureNames() {
		return new FeatureIterator();
	}

	@Override
	public int getFeatureValueAsInt(String featureName) {
		return (int) value.get(featureName).isNumber().doubleValue();
	}

	@Override
	public String getFeatureValueAsString(String featureName) {
		JSONValue val = value.get(featureName);
		if (val != null) {
			return getJsonValueAsString(val);
		}
		return null;
	}

	@Override
	public Annotation getFeatureValueAsAnnotation(String featureName) {
		JSONObject val = value.get(featureName).isObject();
		return getJsonObjectAsAnnotation(val);
	}

	@Override
	public boolean isAnnotation(String featureName) {
		return (value.get(featureName).isObject() != null);
	}

	@Override
	public boolean isDocumentLevel() {
		return (begin < 0 || (begin == 0 && end == cas.getDocumentText().length()));
	}

	@Override
	public boolean isArray() {
		return arrayTypes.contains(getTypeName());
	}

	@Override
	public boolean isNull(String featureName) {
		return (value.get(featureName).isNull() != null);
	}

	@Override
	public String getCoveredText() {
		if (begin < 0) {
			return null;
		}

		return getCas().getDocumentText().substring(begin, end);
	}

	@Override
	public int arraySize() {
		JSONValue jValues = value.get("values");
		if (jValues != null) {
			JSONArray array = jValues.isArray();
			if (array != null) {
				return array.size();
			}
		}
		return 0;
	}

	@Override
	public String getArrayValueAsString(int index) {
		JSONValue jv = value.get("values").isArray().get(index);
		return getJsonValueAsString(jv);
	}

	@Override
	public Annotation getArrayValueAsAnnotation(int index) {
		JSONObject jo = value.get("values").isArray().get(index).isObject();
		return getJsonObjectAsAnnotation(jo);
	}

	private String getJsonValueAsString(JSONValue value) {
		if (value == null) {
			return null;
		} else if (value.isString() != null) {
			return value.isString().stringValue();
		} else if (value.isNumber() != null) {
			return value.isNumber().toString();
		} else if (value.isBoolean() != null) {
			return value.isBoolean().toString();
		}
		return null;
	}

	private Annotation getJsonObjectAsAnnotation(JSONObject value) {
		if (value != null) {
			JSONNumber jsonId;
			if (value.containsKey(JsogConstants.PROPERTY_REF)) {
				jsonId = value.get(JsogConstants.PROPERTY_REF).isNumber();
			} else {
				jsonId = value.get(JsogConstants.PROPERTY_ID).isNumber();
			}

			if (jsonId == null) {
				// Error
			}
			int id = (int) jsonId.doubleValue();
			return cas.getAnnotation(id);
		}
		return null;
	}

	private class FeatureIterator implements Iterator<String> {

		private Iterator<String> it;
		private String next;

		public FeatureIterator() {
			it = value.keySet().iterator();
		}

		@Override
		public boolean hasNext() {
			nextValue();
			return (next != null);
		}

		@Override
		public String next() {
			nextValue();
			String tmp = next;
			next = null;
			return tmp;
		}

		private void nextValue() {
			while (next == null && it.hasNext()) {
				next = it.next();
				if (next.startsWith("@")) {
					next = null;
				}
			}
		}

	}

}

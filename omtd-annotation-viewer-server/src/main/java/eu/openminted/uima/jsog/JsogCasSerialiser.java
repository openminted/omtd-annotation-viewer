package eu.openminted.uima.jsog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CommonArrayFS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.jcas.cas.Sofa;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsogCasSerialiser {

	public void serialise(CAS cas, JsonGenerator json) throws IOException {
		writeCas(cas, json);
	}

	public void serialise(CAS cas, Object output) throws IOException {
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator json = jsonFactory.createGenerator(System.out);
		json.setPrettyPrinter(new DefaultPrettyPrinter());
		writeCas(cas, json);
		json.close();

	}

	private static void writeCas(CAS cas, JsonGenerator json) throws IOException {
		IdGenerator<FeatureStructure> ids = new IdGenerator<>();
		Iterator<CAS> viewIterator = cas.getViewIterator();
		
		json.writeStartArray();
		
		// Write default view first
		writeView(cas, json, ids);
		
		// Write any additional views
		while (viewIterator.hasNext()) {
			CAS view = viewIterator.next();
			if (view != cas) {
				writeView(view, json, ids);
			}
		}

		json.writeEndArray();
	}

	private static void writeView(CAS view, JsonGenerator json, IdGenerator<FeatureStructure> ids) throws IOException {
		json.writeStartObject();
		json.writeFieldName("sofa");
		writeFeatureStructure(view.getSofa(), json, ids);

		FSIterator<FeatureStructure> iterator = view.getIndexRepository()
				.getAllIndexedFS(view.getTypeSystem().getTopType());

		json.writeArrayFieldStart("index");
		while (iterator.hasNext()) {
			FeatureStructure fs = iterator.next();
			writeFeatureStructure(fs, json, ids);
		}
		json.writeEndArray();
		json.writeEndObject();
	}

	private static void writeFeatureStructure(FeatureStructure fs, JsonGenerator json,
			IdGenerator<FeatureStructure> ids) throws IOException {

		if ( fs == null ) {
			json.writeNull();
			return;
		}
		
		if (ids.alreadyHasId(fs)) {
			// Feature Structure has already been written, so simply write
			// reference. E.g. { "@ref": "2" }
			writeReference(json, ids.get(fs));
			return;
		}

		int id = ids.get(fs);
		json.writeStartObject();
		json.writeNumberField("@id", id++);
		json.writeStringField("@type", fs.getType().getName());

		if (fs.getType().isArray()) {
			writeArray(fs, json, ids);
		}

		for (Feature feature : fs.getType().getFeatures()) {
			String fieldName = feature.getShortName();

			if ("sofa".equals(fieldName)) {
				continue;
			}

			if (feature.getRange().isPrimitive()) {
				switch (feature.getRange().getName()) {
				case CAS.TYPE_NAME_BOOLEAN:
					json.writeBooleanField(fieldName, fs.getBooleanValue(feature));
					break;
				case CAS.TYPE_NAME_STRING:
					json.writeStringField(fieldName, fs.getStringValue(feature));
					break;
				case CAS.TYPE_NAME_INTEGER:
				case CAS.TYPE_NAME_DOUBLE:
				case CAS.TYPE_NAME_FLOAT:
				case CAS.TYPE_NAME_LONG:
				case CAS.TYPE_NAME_SHORT:
					json.writeFieldName(fieldName);
					json.writeNumber(fs.getFeatureValueAsString(feature));
					break;
				case CAS.TYPE_NAME_BYTE:
					// TODO
					break;
				}
			} else {
				FeatureStructure fsv = fs.getFeatureValue(feature);
				if (fsv != null) {
					json.writeFieldName(fieldName);
					writeFeatureStructure(fsv, json, ids);
				}
			}
		}

		json.writeEndObject();
	}

	private static void writeArray(FeatureStructure fs, JsonGenerator json, IdGenerator<FeatureStructure> ids)
			throws IOException {

		json.writeFieldName("values");
		json.writeStartArray();

		switch (fs.getType().getName()) {
		case CAS.TYPE_NAME_BOOLEAN_ARRAY:
			boolean[] booleanArray = ((BooleanArrayFS) fs).toArray();
			for (boolean val : booleanArray) {
				json.writeBoolean(val);
			}
			break;
		case CAS.TYPE_NAME_DOUBLE_ARRAY:
		case CAS.TYPE_NAME_FLOAT_ARRAY:
		case CAS.TYPE_NAME_INTEGER_ARRAY:
		case CAS.TYPE_NAME_LONG_ARRAY:
		case CAS.TYPE_NAME_SHORT_ARRAY:
			String[] numberArray = ((CommonArrayFS) fs).toStringArray();
			for (String val : numberArray) {
				json.writeNumber(val);
			}
			break;
		case CAS.TYPE_NAME_STRING_ARRAY:
			String[] stringArray = ((CommonArrayFS) fs).toStringArray();
			for (String val : stringArray) {
				json.writeString(val);
			}
			break;
		case CAS.TYPE_NAME_FS_ARRAY:
			ArrayFS fsArray = (ArrayFS) fs;
			for (int i = 0; i < fsArray.size(); i++) {
				FeatureStructure val = fsArray.get(i);
				writeFeatureStructure(val, json, ids);
			}
			break;
		}

		json.writeEndArray();
	}

	private static void writeReference(JsonGenerator json, Integer id) throws IOException {
		json.writeStartObject();
		json.writeNumberField("@ref", id);
		json.writeEndObject();
	}

	static class IdGenerator<T> {
		private final Map<T, Integer> fsMap = new HashMap<>();
		private int id = 0;

		public boolean alreadyHasId(T t) {
			return fsMap.containsKey(t);
		}

		public int get(T t) {
			Integer existingId = fsMap.get(t);
			if (existingId == null) {
				id++;
				fsMap.put(t, id);
				return id;
			} else {
				return existingId;
			}
		}
	}
}

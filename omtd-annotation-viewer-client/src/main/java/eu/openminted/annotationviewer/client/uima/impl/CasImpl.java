package eu.openminted.annotationviewer.client.uima.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;

import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.uima.Sofa;

public class CasImpl implements Cas {

	private final Map<Integer, Annotation> annotations = new HashMap<>();
	private final Set<Annotation> annotationIndex = new HashSet<>();
	private final Map<String, Cas> viewMap;
	private final String viewName;
	private Sofa sofa;

	public CasImpl() {
		this(Cas.NAME_DEFAULT_SOFA, new HashMap<String, Cas>());
	}

	protected CasImpl(String viewName, Map<String, Cas> viewMap) {
		this.viewName = viewName;
		this.viewMap = viewMap;
		viewMap.put(viewName, this);
	}

	@Override
	public Iterator<Annotation> getIndexedAnnotations() {
		return annotationIndex.iterator();
	}

	@Override
	public Iterator<Annotation> getAllAnnotations() {
		return annotations.values().iterator();
	}

	@Override
	public Iterator<Annotation> getAllAnnotations(String typeName) {
		// TODO cache these?
		List<Annotation> list = new ArrayList<>();
		for (Annotation ann : annotations.values()) {
			if (typeName.equals(ann.getTypeName())) {
				list.add(ann);
			}
		}
		return list.iterator();
	}

	public Annotation createAnnotation(JSONObject jsonAnnotation) {
		Annotation annotation = new AnnotationImpl(jsonAnnotation, this);

		annotations.put(annotation.getId(), annotation);

		return annotation;
	}

	@Override
	public void addAnnotationToIndex(Annotation annotation) {
		if (!annotation.getCas().equals(this)) {
			throw new IllegalArgumentException(
					"Trying to add annotation to CAS annotation index but it exists in another CAS");
		}

		annotationIndex.add(annotation);
	}

	@Override
	public CasImpl createView(String name) {
		if (viewMap.containsKey(name)) {
			throw new IllegalArgumentException("Trying to add a view to a CAS with the name '" + name
					+ "' however a view with this name already exists");
		}
		return new CasImpl(name, viewMap);
	}

	@Override
	public Cas getView(String name) {
		return viewMap.get(name);
	}

	@Override
	public String getViewName() {
		return viewName;
	}

	@Override
	public Iterator<Cas> getViewIterator() {
		return viewMap.values().iterator();
	}

	@Override
	public Sofa getSofa() {
		return sofa;
	}

	@Override
	public String getDocumentText() {
		if (sofa != null) {
			return sofa.getLocalStringData();
		}
		return null;
	}

	// For internal use
	public void setSofa(Sofa sofa) {
		if (sofa.getCas() != this) {
			throw new IllegalArgumentException("Trying to set a sofa on a CAS however it belongs to a different CAS");
		} else if (!viewName.equals(sofa.getSofaID())) {
			throw new IllegalArgumentException(
					"Trying to set a sofa on a CAS however the 'sofaID' doesn't match the CAS view name");
		}
		this.sofa = sofa;
	}

	// For internal use
	public Annotation getAnnotation(int id) {
		return annotations.get(id);
	}

}

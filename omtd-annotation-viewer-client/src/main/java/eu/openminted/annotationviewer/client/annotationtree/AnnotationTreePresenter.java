package eu.openminted.annotationviewer.client.annotationtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import eu.openminted.annotationviewer.client.annotationtree.event.ViewAnnotationInTextEvent;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyle;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;
import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;

public class AnnotationTreePresenter extends PresenterWidget<AnnotationTreePresenter.MyView>
		implements AnnotationTypeStyles.Handler, AnnotationTreeUiHandlers {
	interface MyView extends View, HasUiHandlers<AnnotationTreeUiHandlers> {
		void showAnnotationValue(Annotation root, List<Annotation> path, AnnotationTypeStyles styles);

		void addAnnotation(Annotation annotation, AnnotationTypeStyles styles);

		void setAnnotationColors(int id, String foregroundColor, String backgroundColor);

		void scrollToAnnotation(int id);

		void setAnnotationVisibility(int id, boolean visible);

		void clear();
	}

	private Cas cas;
	private AnnotationTypeStyles typeStyles;

	@Inject
	AnnotationTreePresenter(EventBus eventBus, MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}

	@Override
	public void viewAnnotationInText(int id) {
		ViewAnnotationInTextEvent.fire(this, id);
	}

	@Override
	public void viewAnnotationValue(int rootId, List<Integer> pathIds) {
		Annotation root = cas.getAnnotation(rootId);
		List<Annotation> previous = new ArrayList<>();
		if (pathIds != null) {
			for (int id : pathIds) {
				previous.add(cas.getAnnotation(id));
			}
		}
		getView().showAnnotationValue(root, previous, typeStyles);
	}

	public void setCas(Cas cas, AnnotationTypeStyles typeStyles) {
		
		if (true) {
			//return;
		}
		
		if (this.typeStyles != null) {
			typeStyles.removeHandler(this);
		}
		this.cas = cas;
		this.typeStyles = typeStyles;
		typeStyles.addHandler(this);

		getView().clear();

		// Sort annotations
		Iterator<Annotation> it = cas.getAllAnnotations();
		List<Annotation> annotations = new ArrayList<>();
		while (it.hasNext()) {
			// TODO don't show non-indexed document level annotations?
			annotations.add(it.next());
		}

		Collections.sort(annotations, new AnnotationComparator());
		for (Annotation annotation : annotations) {
			getView().addAnnotation(annotation, typeStyles);
		}
	}

	public void setAnnotationsVisibility(boolean visible) {
		setAnnotationsVisibility(cas.getAllAnnotations(), visible);
	}

	public void setAnnotationTypeVisibility(String typeName, boolean visible) {
		setAnnotationsVisibility(cas.getAllAnnotations(typeName), visible);
	}

	private void setAnnotationsVisibility(Iterator<Annotation> it, boolean visible) {
		while (it.hasNext()) {
			Annotation ann = it.next();
			getView().setAnnotationVisibility(ann.getId(), visible);
		}
	}

	public void scrollAnnotationIntoView(int id) {
		getView().scrollToAnnotation(id);
	}

	class AnnotationComparator implements Comparator<Annotation> {
		@Override
		public int compare(Annotation ann1, Annotation ann2) {
			if (ann1.isDocumentLevel()) {
				return -1;
			} else if (ann2.isDocumentLevel()) {
				return 1;
			} else {
				int val = ann1.getBegin() - ann2.getBegin();
				if (val == 0) {
					val = ann1.getEnd() - ann2.getEnd();
				}
				return val;
			}
		}
	}

	@Override
	public void onChange(String typeName) {
		Iterator<Annotation> it = cas.getAllAnnotations(typeName);
		AnnotationTypeStyle style = typeStyles.getStyle(typeName);
		while (it.hasNext()) {
			Annotation ann = it.next();
			getView().setAnnotationColors(ann.getId(), style.getForegroundColor(), style.getBackgroundColor());
		}
	}

}

package eu.openminted.annotationviewer.client.viewer;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import eu.openminted.annotationviewer.client.style.AnnotationTypeStyle;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;
import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.viewer.event.AnnotationsSelectedEvent;

public class DocumentPresenter extends PresenterWidget<DocumentPresenter.MyView>
		implements AnnotationTypeStyles.Handler, DocumentUiHandlers {

	interface MyView extends View, HasUiHandlers<DocumentUiHandlers> {
		// void clear();

		void setText(String text);

		void addAnnotation(int id, int start, int end, String foregroundColor, String backgroundColor, boolean visible);

		void redrawAnnotations();

		void setAnnotationColors(int id, String foregroundColor, String backgroundColor);

		void setAnnotationVisibility(int id, boolean visible);

		void scrollToAnnotation(int id);
	}

	private Cas cas;
	private AnnotationTypeStyles typeStyles;

	@Inject
	public DocumentPresenter(EventBus eventBus, MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}

	public void setCas(Cas cas, AnnotationTypeStyles typeStyles) {
		if (this.typeStyles != null) {
			typeStyles.removeHandler(this);
		}

		this.cas = cas;
		this.typeStyles = typeStyles;
		typeStyles.addHandler(this);

		getView().setText(cas.getDocumentText());

		Iterator<Annotation> annotationIt = cas.getAllAnnotations();
		while (annotationIt.hasNext()) {
			Annotation ann = annotationIt.next();
			if (!ann.isDocumentLevel()) {
				int begin = ann.getBegin();
				int end = ann.getEnd();

				AnnotationTypeStyle style = typeStyles.getStyle(ann.getTypeName());

				getView().addAnnotation(ann.getId(), begin, end, style.getForegroundColor(), style.getBackgroundColor(),
						style.isVisible());
			}
		}

		/*
		  getView().addAnnotation(5000, 4, 10, "black", "yellow", true);
		  getView().addAnnotation(5001, 6, 12, "black", "green", true);
		  getView().addAnnotation(5002, 1, 14, "black", "red", true);
		  getView().addAnnotation(5003, 0, 11, "black", "white", true);
		  getView().addAnnotation(5004, 2, 16, "black", "aqua", true);
		  getView().addAnnotation(5005, 0, 15, "black", "pink", true);
		  getView().addAnnotation(5006, 3, 9, "black", "pink", true);
		  */
		 

		getView().redrawAnnotations();
	}

	public void scrollAnnotationIntoView(int id) {
		getView().scrollToAnnotation(id);
	}

	public void setAnnotationTypeVisibility(String typeName, boolean visible) {
		setAnnotationsVisibility(cas.getAllAnnotations(typeName), visible);
	}

	public void setAnnotationsVisibility(boolean visible) {
		setAnnotationsVisibility(cas.getAllAnnotations(), visible);
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

	@Override
	public void onAnnotationsSelected(List<Integer> ids) {
		AnnotationsSelectedEvent.fire(this, ids);
	}

	private void setAnnotationsVisibility(Iterator<Annotation> it, boolean visible) {
		while (it.hasNext()) {
			Annotation ann = it.next();
			getView().setAnnotationVisibility(ann.getId(), visible);
		}
		getView().redrawAnnotations();
	}
}

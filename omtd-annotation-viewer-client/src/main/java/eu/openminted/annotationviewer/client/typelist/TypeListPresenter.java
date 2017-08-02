package eu.openminted.annotationviewer.client.typelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import eu.openminted.annotationviewer.client.style.AnnotationTypeStyle;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;
import eu.openminted.annotationviewer.client.typelist.event.SetTypeVisibilityEvent;
import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;

public class TypeListPresenter extends PresenterWidget<TypeListPresenter.MyView> implements TypeListUiHandlers {
	interface MyView extends View, HasUiHandlers<TypeListUiHandlers> {
		void clear();

		void addType(String typeName, String backgroundColor, String foregroundColor, boolean visible);
	}

	private Cas cas;

	@Inject
	public TypeListPresenter(EventBus eventBus, MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}

	public void setCas(Cas cas, AnnotationTypeStyles typeStyles) {
		this.cas = cas;

		getView().clear();

		Iterator<Annotation> it = cas.getAllAnnotations();
		Set<String> typeNames = new HashSet<>();
		while (it.hasNext()) {
			Annotation ann = it.next();
			// if (!ann.isDocumentLevel()) {
			typeNames.add(ann.getTypeName());
			// }
		}

		List<String> sortedTypeNames = new ArrayList<>(typeNames);
		Collections.sort(sortedTypeNames, new TypeComparator());

		for (String typeName : sortedTypeNames) {
			AnnotationTypeStyle style = typeStyles.getStyle(typeName);
			getView().addType(typeName, style.getBackgroundColor(), style.getForegroundColor(), style.isVisible());
		}
	}

	private static class TypeComparator implements Comparator<String> {
		@Override
		public int compare(String t1, String t2) {
			if (t1.indexOf('.') >= 0) {
				t1 = t1.substring(t1.lastIndexOf(".") + 1);
			}

			if (t2.indexOf('.') >= 0) {
				t2 = t2.substring(t2.lastIndexOf(".") + 1);
			}

			return t1.compareTo(t2);
		}

	}

	@Override
	public void setTypeVisibility(String typeName, boolean visible) {
		SetTypeVisibilityEvent.fire(this, typeName, visible);
	}
}

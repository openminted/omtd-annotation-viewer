package eu.openminted.annotationviewer.client.annotationtree;

import java.util.List;

import com.gwtplatform.mvp.client.UiHandlers;

public interface AnnotationTreeUiHandlers  extends UiHandlers{
	void viewAnnotationInText(int id);
	void viewAnnotationValue(int rootId, List<Integer> pathIds);
}

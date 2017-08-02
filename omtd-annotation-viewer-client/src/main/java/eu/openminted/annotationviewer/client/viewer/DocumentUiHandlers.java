package eu.openminted.annotationviewer.client.viewer;

import java.util.List;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DocumentUiHandlers  extends UiHandlers {
	void onAnnotationsSelected(List<Integer> ids);
}

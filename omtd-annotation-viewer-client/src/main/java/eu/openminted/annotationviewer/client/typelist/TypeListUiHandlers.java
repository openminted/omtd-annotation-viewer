package eu.openminted.annotationviewer.client.typelist;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TypeListUiHandlers extends UiHandlers {
	void setTypeVisibility(String typeName, boolean visible);
}

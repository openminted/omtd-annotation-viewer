package eu.openminted.annotationviewer.client.application;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ApplicationUiHandlers extends UiHandlers {
	void onViewChange(String viewId);
	void onSearchAutoSuggest(String query);
	
}

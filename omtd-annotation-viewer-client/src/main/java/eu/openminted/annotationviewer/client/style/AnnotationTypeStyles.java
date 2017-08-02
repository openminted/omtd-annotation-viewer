package eu.openminted.annotationviewer.client.style;

import java.util.Iterator;
import java.util.List;

public interface AnnotationTypeStyles {
	interface Handler {
		void onChange(String typeName);
	}
	
	void addHandler(Handler handler);
	void removeHandler(Handler handler);
	
	AnnotationTypeStyle getStyle(String typeName);
	
	Iterator<AnnotationTypeStyle> getStyles();
	
}

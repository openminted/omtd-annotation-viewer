package eu.openminted.annotationviewer.client.style;

public interface AnnotationTypeStyle {
	void setColor(String backgroundColor, String foregroundColor);
	String getForegroundColor();
	String getBackgroundColor();
	String getTypeName();
	void setVisible(boolean visible);
	boolean isVisible();
	
}

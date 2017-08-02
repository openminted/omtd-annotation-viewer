package eu.openminted.annotationviewer.client.style.impl;

import java.util.Set;

import eu.openminted.annotationviewer.client.style.AnnotationTypeStyle;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles.Handler;

public class AnnotationTypeStyleImpl implements AnnotationTypeStyle {

	private final String typeName;
	private final Set<AnnotationTypeStyles.Handler> handlers;
	private String foregroundColor;
	private String backgroundColor;
	private boolean visible = true;


	AnnotationTypeStyleImpl(String typeName, String backgroundColor, String foregroundColor,
			Set<AnnotationTypeStyles.Handler> handlers) {
		this.handlers = handlers;
		this.typeName = typeName;
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
	}

	@Override
	public void setColor(String backgroundColor, String foregroundColor) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		notifyHandlers(typeName);
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public String getForegroundColor() {
		return foregroundColor;
	}

	@Override
	public String getBackgroundColor() {
		return backgroundColor;
	}
	
	@Override
	public String getTypeName() {
		return typeName;
	}
	
	void notifyHandlers(String typeName) {
		for (Handler h : handlers) {
			h.onChange(typeName);
		}
	}
}

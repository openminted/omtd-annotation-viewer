package eu.openminted.annotationviewer.client.viewer;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;

class ViewAnnotation implements Comparable<ViewAnnotation> {

	private final int id;

	private final int start;

	private final int end;

	private int level = 0;

	private List<Element> elements = new ArrayList<Element>();

	private boolean highlighted = false;

	private String backgroundColor;
	private String foregroundColor;
	private boolean visible = true;

	public ViewAnnotation(int id, int start, int end) {
		this.id = id;
		this.start = start;
		this.end = end;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public int getId() {
		return id;
	}

	List<Element> getElements() {
		return elements;
	}

	public void setForegroundColor(String foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getForegroundColor() {
		return foregroundColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return start + ":" + end;
	}

	@Override
	public int hashCode() {
		// return toString().hashCode();
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ViewAnnotation)) {
			return false;
		}
		ViewAnnotation ann = (ViewAnnotation) o;
		// return (ann.start == start) && (ann.end == end);
		return ann.id == id;
	}

	public int compareTo(ViewAnnotation ann) {
		if (start < ann.start || (start == ann.start && end > ann.end)) {
			return -1;
		} else if (start == ann.start && end == ann.end) {
			return 0;
		} else {
			return 1;
		}

		/*
		 * if (end > ann.end) return 1; if (end < ann.end) return -1; if (start
		 * < ann.start) return 1; if (start > ann.start) return -1; return id -
		 * ann.id;
		 */
	}

}

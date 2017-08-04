package eu.openminted.annotationviewer.client.viewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;

class ViewAnnotation  {
	
	private static int textSize;

	private final int id;

	private final int start;

	private final int end;

	private int level = -1;

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
	
	public static void setTextSize(int textSize) {
		ViewAnnotation.textSize = textSize;
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
	
	public void clear() {
		elements.clear();
		level = -1;
	}
	
	public void addElement(Element elem) {
		elements.add(elem);
		styleElement(elem);
	}
	
	public Iterator<Element> getElementsIterator() {
		return elements.iterator();
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
		for (Element elem: elements) {
			styleElement(elem);
		}
	}
	
	void styleElement(Element elem) {
		int padding = ((level + 1) * 3) - 1;
		int lineHeight = (2 * padding) + textSize + 12;
		Style style = elem.getStyle();
		style.setPaddingTop(padding, Unit.PX);
		style.setPaddingBottom(padding, Unit.PX);
		style.setLineHeight(lineHeight, Unit.PX);
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
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ViewAnnotation)) {
			return false;
		}
		return ( id == ((ViewAnnotation) obj).id);
	}


}

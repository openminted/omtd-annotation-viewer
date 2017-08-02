package eu.openminted.annotationviewer.client.viewer;

import com.google.gwt.dom.client.Text;

public class EndPoint {
	
	private Text text = null;
	
	private int offset = 0;
	
	public EndPoint(Text text, int offset) {
		this.text = text;
		this.offset = offset;
	}
	
	public Text getText() {
		return text;
	}
	
	public int getOffset() {
		return offset;
	}

}

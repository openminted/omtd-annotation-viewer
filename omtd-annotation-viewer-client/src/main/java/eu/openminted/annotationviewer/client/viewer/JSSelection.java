package eu.openminted.annotationviewer.client.viewer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Node;

public class JSSelection extends JavaScriptObject {
	
	protected JSSelection() {
		// Does Nothing
	}
	
	public static final native JSSelection getSelection() /*-{
		var userSelection;
		if ($wnd.getSelection) {
			userSelection = $wnd.getSelection();
		}
		else if ($doc.getSelection) { // should come last; Opera!
			userSelection = $doc.getSelection();
		}
		return userSelection;
	}-*/;
	
	public final native Node getStartNode() /*-{
		return this.anchorNode;
	}-*/;
	
	public final native int getStartOffset() /*-{
		return this.anchorOffset;
	}-*/;
	
	public final native Node getEndNode() /*-{
		return this.focusNode;
	}-*/;
	
	public final native int getEndOffset() /*-{
		return this.focusOffset;
	}-*/;
	
	public final native void clear() /*-{
		this.removeAllRanges();
	}-*/;
}

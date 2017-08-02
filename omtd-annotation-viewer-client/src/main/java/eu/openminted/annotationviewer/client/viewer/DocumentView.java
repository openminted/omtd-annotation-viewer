package eu.openminted.annotationviewer.client.viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Text;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class DocumentView extends ViewWithUiHandlers<DocumentUiHandlers> implements DocumentPresenter.MyView {
	interface Binder extends UiBinder<HTMLPanel, DocumentView> {
	}

	interface MyStyle extends CssResource {
		String annotation();

		String annotationBegin();

		String annotationEnd();
	}

	@UiField
	MyStyle style;

	@UiField
	HTML main;

	@UiField
	HTML bottom;

	private Element element;

	private String text = null;

	private Map<Integer, ViewAnnotation> annotationMap = new HashMap<>();

	// This is a set of annotations that occur at each position in the text
	private List<Set<ViewAnnotation>> annotationIndex = new ArrayList<Set<ViewAnnotation>>();

	// A count of how many annotations start or end at any given position
	private List<Integer> beginEndPositions = new ArrayList<Integer>();

	private boolean updated = false;

	@Inject
	public DocumentView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void scrollToAnnotation(int id) {
		ViewAnnotation ann = annotationMap.get(id);
		if (ann != null) {
			// Scroll to bottom item in the annotation list. This ensures when
			// scrolling to the selected item that it appears at the top of the
			// page. See:
			// https://stackoverflow.com/questions/39377524/scrolltoview-brings-the-element-at-the-bottom-how-to-scroll-element-to-the-to
			bottom.getElement().scrollIntoView();

			ann.getElements().get(0).scrollIntoView();
		}

	}

	@Override
	public void setAnnotationColors(int id, String foregroundColor, String backgroundColor) {
		ViewAnnotation ann = annotationMap.get(id);
		ann.setForegroundColor(foregroundColor);
		ann.setBackgroundColor(backgroundColor);
		for (Element element : ann.getElements()) {
			element.getStyle().setBackgroundColor(backgroundColor);
			element.getStyle().setColor(foregroundColor);
		}
	}

	@Override
	public void setText(String text) {
		clear();
		this.text = text;
		main.setText(text);
		for (int i = 0; i < text.length(); i++) {
			annotationIndex.add(new HashSet<ViewAnnotation>());
			beginEndPositions.add(0);
		}

	}

	private void clear() {
		text = null;
		annotationIndex.clear();
		annotationMap.clear();
		beginEndPositions.clear();

		// Add an extra for the text length
		beginEndPositions.add(0);
		updated = false;
	}

	@Override
	public void addAnnotation(int id, int start, int end, String foregroundColor, String backgroundColor,
			boolean visible) {
		if (start >= end) {
			return;
		}
		if (end > text.length()) {
			end = text.length();
		}

		// Add the annotation at the positions between start and end
		ViewAnnotation ann = new ViewAnnotation(id, start, end);
		ann.setForegroundColor(foregroundColor);
		ann.setBackgroundColor(backgroundColor);
		ann.setVisible(visible);
		annotationMap.put(id, ann);
		if (ann.isVisible()) {
			visibilityChange(ann);
		}
	}

	@Override
	public void setAnnotationVisibility(int id, boolean visible) {
		ViewAnnotation ann = annotationMap.get(id);
		if (ann != null && ann.isVisible() != visible) {
			ann.setVisible(visible);
			visibilityChange(ann);
		}
	}

	private void visibilityChange(ViewAnnotation ann) {
		int start = ann.getStart();
		int end = ann.getEnd();

		if (ann.isVisible()) {
			for (int i = start; i < end; i++) {
				annotationIndex.get(i).add(ann);
			}

			beginEndPositions.set(start, beginEndPositions.get(start) + 1);
			beginEndPositions.set(end, beginEndPositions.get(end) + 1);

		} else {
			for (int i = start; i < end; i++) {
				annotationIndex.get(i).remove(ann);
			}
			beginEndPositions.set(start, beginEndPositions.get(start) - 1);
			beginEndPositions.set(end, beginEndPositions.get(end) - 1);
			ann.getElements().clear();
		}
		updated = true;
	}

	@Override
	public void redrawAnnotations() {
		if (updated) {
			updateAnnotations(false);
		}
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		this.element = main.getElement();
		if (text != null) {
			updateAnnotations(true);
		}
	}


	private boolean updateAnnotations(boolean force) {
		if (!force && (!main.isAttached() || text == null || annotationIndex.isEmpty())) {
			// This widget must be attached before annotation rendering
			// calculations can be performed.
			return false;
		}

		// Clear the text
		element.setInnerHTML("");

		// Clear the existing elements within the annotations
		for (ViewAnnotation ann : annotationMap.values()) {
			ann.getElements().clear();
		}

		// calculateLevels(start, end);
		calculateLevels();

		// Computed values are only available once a widget is attached.
		int textSize = (int) Math.ceil(getComputedFontSize(element));

		// Go through the text and add spans
		for (int i = 0; i < text.length(); i++) {

			// Find the next position where something changes
			int spanEnd = i + 1;
			int spanBegin = i;

			while ((spanEnd < text.length()) && (beginEndPositions.get(spanEnd) <= 0)) {
				spanEnd++;
				i++;
			}

			// Get the annotations that occur in this span ordered by level
			Set<ViewAnnotation> currentAnnotationSet = new HashSet<ViewAnnotation>();
			for (int j = spanBegin; j < spanEnd; j++) {
				currentAnnotationSet.addAll(annotationIndex.get(j));
			}
			List<ViewAnnotation> currentAnnotations = new ArrayList<ViewAnnotation>(currentAnnotationSet);
			Collections.sort(currentAnnotations, new Comparator<ViewAnnotation>() {
				public int compare(ViewAnnotation a1, ViewAnnotation a2) {
					return a2.getLevel() - a1.getLevel();
				}
			});

			// Insert spans
			Element parent = element;
			int leftMargin = 0;
			int rightMargin = 0;
			for (int j = 0; j < currentAnnotations.size(); j++) {

				ViewAnnotation ann = currentAnnotations.get(j);

				// Insert the annotation level
				SpanElement span = Document.get().createSpanElement();

				// span.setAttribute("data-begin", "" + spanBegin);
				// span.setAttribute("data-end", "" + spanEnd);
				span.setAttribute("data-id", "" + ann.getId());

				// Calculated style - can't be in stylesheet
				int padding = ((ann.getLevel() + 1) * 3) - 1;
				int lineHeight = (2 * padding) + textSize + 12;

				Style style = span.getStyle();
				span.addClassName(this.style.annotation());
				if (ann.getBackgroundColor() != null) {
					style.setBackgroundColor(ann.getBackgroundColor());
				}
				if (ann.getForegroundColor() != null) {
					style.setColor(ann.getForegroundColor());
				}
				style.setPaddingTop(padding, Unit.PX);
				style.setPaddingBottom(padding, Unit.PX);
				style.setLineHeight(lineHeight, Unit.PX);
				if (spanBegin != ann.getStart()) {
					// Setting the margin hides the border(s) of any annotations
					// starting at the character position represented by this
					// span
					if (leftMargin < 0) {
						style.setMarginLeft(leftMargin, Unit.PX);
						leftMargin = 0;
					}
				} else {
					// This span represents the start of an annotation
					span.addClassName(this.style.annotationBegin());
					leftMargin--;
				}
				if (spanEnd != ann.getEnd()) {
					// Setting the margin hides the border(s) of any annotations
					// ending at the character position represented by this span
					if (rightMargin > 0) {
						style.setMarginRight(rightMargin, Unit.PX);
						rightMargin = 0;
					}
				} else {
					// This span represents the end of an annotation
					span.addClassName(this.style.annotationEnd());
					rightMargin++;
				}

				ann.getElements().add(span);
				parent.appendChild(span);
				parent = span;
			}

			// Insert text
			String spanText = text.substring(spanBegin, spanEnd);
			int lastEnd = 0;
			int nextBreakIndex = spanText.indexOf('\n', lastEnd);
			while (nextBreakIndex >= 0) {
				if ((nextBreakIndex - lastEnd) >= 0) {
					String string = spanText.substring(lastEnd, nextBreakIndex + 1);
					parent.appendChild(Document.get().createTextNode(string));
				}
				parent.appendChild(Document.get().createBRElement());
				lastEnd = nextBreakIndex + 1;
				nextBreakIndex = spanText.indexOf('\n', lastEnd);
			}
			if (lastEnd < spanText.length()) {
				String substring = spanText.substring(lastEnd, spanText.length());
				parent.appendChild(Document.get().createTextNode(substring));
			}
		}
		updated = false;
		return true;
	}

	private void calculateLevels() {
		// Calculate the levels of each of the annotations
		List<SortedSet<Integer>> levels = new ArrayList<SortedSet<Integer>>();
		for (ViewAnnotation ann : annotationMap.values()) {

			// Find a level that doesn't contain any annotations
			int freeLevel = 0;
			while (freeLevel < levels.size()) {
				Set<Integer> levelPositions = levels.get(freeLevel);
				boolean found = true;
				for (int i = ann.getStart(); i < ann.getEnd(); i++) {
					if (levelPositions.contains(i)) {
						found = false;
						break;
					}
				}
				if (found) {
					break;
				}
				freeLevel++;
			}

			// Update the level with the positions of the annotation
			while (freeLevel >= levels.size()) {
				levels.add(new TreeSet<Integer>());
			}
			SortedSet<Integer> levelPositions = levels.get(freeLevel);
			for (int i = ann.getStart(); i < ann.getEnd(); i++) {
				levelPositions.add(i);
			}
			ann.setLevel(freeLevel);
		}
	}

	private void calculateLevels(int start, int end) {
		if (start >= end) {
			return;
		}
		if (end > text.length()) {
			end = text.length();
		}

		// Find the position prior to the start that contains no annotations
		int replaceStart = start;
		while ((replaceStart > 0) && !annotationIndex.get(replaceStart - 1).isEmpty()) {
			replaceStart--;
		}

		// Find the position after the end that contains no annotations
		int replaceEnd = end;
		while ((replaceEnd < text.length()) && !annotationIndex.get(replaceEnd).isEmpty()) {
			replaceEnd++;
		}

		// Get all annotations in the range
		SortedSet<ViewAnnotation> annotations = new TreeSet<ViewAnnotation>();
		for (int i = replaceStart; i < replaceEnd; i++) {
			annotations.addAll(annotationIndex.get(i));
		}

		// Calculate the levels of each of the annotations
		List<SortedSet<Integer>> levels = new ArrayList<SortedSet<Integer>>();
		for (ViewAnnotation ann : annotations) {

			// Find a level that doesn't contain any annotations
			int freeLevel = 0;
			while (freeLevel < levels.size()) {
				Set<Integer> levelPositions = levels.get(freeLevel);
				boolean found = true;
				for (int i = ann.getStart(); i < ann.getEnd(); i++) {
					if (levelPositions.contains(i)) {
						found = false;
						break;
					}
				}
				if (found) {
					break;
				}
				freeLevel++;
			}

			// Update the level with the positions of the annotation
			while (freeLevel >= levels.size()) {
				levels.add(new TreeSet<Integer>());
			}
			SortedSet<Integer> levelPositions = levels.get(freeLevel);
			for (int i = ann.getStart(); i < ann.getEnd(); i++) {
				levelPositions.add(i);
			}
			ann.setLevel(freeLevel);
		}
	}

	@UiHandler("main")
	void onMouseUp(ClickEvent event) {
		
		EventTarget target = event.getNativeEvent().getEventTarget();
		if (!Element.is(target)) {
			return;
		}

		Element e = target.cast();
		if (e == element) {
			return;
		}

		List<Integer> ids = new ArrayList<Integer>();
		do {
			if (e.hasAttribute("data-id")) {
				try {
					int id = Integer.parseInt(e.getAttribute("data-id"));
					ids.add(id);
				} catch (NumberFormatException ex) {
					// TODO log
				}
			}

			e = e.getParentElement();
		} while (e != element);

		if (ids.size() > 0) {
			getUiHandlers().onAnnotationsSelected(ids);
		}
	}

	// UiHandler("main")
	void onMouseUpOriginal(ClickEvent event) {
		JSSelection selection = JSSelection.getSelection();

		if (selection.getStartNode() == null) {
			return;
		}
		int begin = findOffset(selection.getStartNode(), selection.getStartOffset());
		Set<ViewAnnotation> set = annotationIndex.get(begin);
		if (!set.isEmpty()) {
			List<Integer> ids = new ArrayList<Integer>(set.size());
			for (ViewAnnotation ann : set) {
				if (ann.isVisible()) {
					ids.add(ann.getId());
				}
			}
			Window.alert(begin + " " + ids.size() + "");

			if (!ids.isEmpty()) {
				getUiHandlers().onAnnotationsSelected(ids);
			}
		}
	}

	private int findOffset(Node findNode, int nodeOffset) {

		if (findNode.getNodeType() != Node.TEXT_NODE) {
			Element findElement = (Element) findNode;
			findNode = findElement.getChild(nodeOffset);
			nodeOffset = 0;
		}

		Node node = element.getFirstChild();
		int count = 0;
		while ((node != null) && (node != findNode)) {
			if (node.getNodeType() == Node.TEXT_NODE) {
				Text text = Text.as(node);
				count += text.getData().length();
			}

			if (node.hasChildNodes()) {
				node = node.getFirstChild();
			} else {
				node = getNextNode(element, node);
			}
		}
		if (node == null) {
			return -1;
		}

		return nodeOffset + count;
	}

	private Node getNextNode(Node topLevel, Node node) {
		if (node == topLevel) {
			return null;
		}
		Node next = node.getNextSibling();
		if (next != null) {
			return next;
		}
		return getNextNode(topLevel, node.getParentNode());
	}

	static native float getComputedFontSize(Element element) /*-{
																if ($doc.defaultView && $doc.defaultView.getComputedStyle) {
																var style = $doc.defaultView.getComputedStyle(element, null).getPropertyValue("font-size");
																return parseFloat(style);
																}
																return 0;
																}-*/;

}

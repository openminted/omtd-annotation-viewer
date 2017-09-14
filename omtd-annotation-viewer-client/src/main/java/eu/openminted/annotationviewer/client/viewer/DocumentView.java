package eu.openminted.annotationviewer.client.viewer;

import java.lang.Character.UnicodeScript;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
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

import eu.openminted.annotationviewer.client.uima.Annotation;

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

			Iterator<Element> it = ann.getElementsIterator();
			if (it.hasNext()) {
				it.next().scrollIntoView();
			}
		}
	}

	@Override
	public void setAnnotationColors(int id, String foregroundColor, String backgroundColor) {
		ViewAnnotation ann = annotationMap.get(id);
		ann.setForegroundColor(foregroundColor);
		ann.setBackgroundColor(backgroundColor);
		Iterator<Element> it = ann.getElementsIterator();
		while (it.hasNext()) {
			Element elem = it.next();
			elem.getStyle().setBackgroundColor(backgroundColor);
			elem.getStyle().setColor(foregroundColor);
		}
	}

	@Override
	public void setText(String text) {
		clear();
		this.text = text;
		main.setText(text);
	}

	private void clear() {
		text = null;
		//element.setInnerHTML("");
		annotationMap.clear();
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
		end--;

		// Add the annotation at the positions between start and end
		ViewAnnotation ann = new ViewAnnotation(id, start, end);
		ann.setForegroundColor(foregroundColor);
		ann.setBackgroundColor(backgroundColor);
		ann.setVisible(visible);
		annotationMap.put(id, ann);
		updated = true;
	}

	@Override
	public void setAnnotationVisibility(int id, boolean visible) {
		ViewAnnotation ann = annotationMap.get(id);
		if (ann != null && ann.isVisible() != visible) {
			ann.setVisible(visible);
			updated = true;
		}
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
		if (!force && (!main.isAttached() || text == null)) {
			// This widget must be attached before annotation rendering
			// calculations can be performed.
			return false;
		}

		// Clear the text
		element.setInnerHTML("");

		int textSize = (int) Math.ceil(getComputedFontSize(element));
		ViewAnnotation.setTextSize(textSize);

		// Clear the existing elements within the annotations
		List<ViewAnnotation> visibleAnnotations = new ArrayList<>();
		for (ViewAnnotation ann : annotationMap.values()) {
			ann.clear();
			if ( ann.isVisible() ) {
				visibleAnnotations.add(ann);
			}
		}

		List<ViewAnnotation> beginAnnotations = new ArrayList<>(visibleAnnotations);
		Collections.sort(beginAnnotations, new Comparator<ViewAnnotation>() {
			public int compare(ViewAnnotation a1, ViewAnnotation a2) {
				if (a1.getStart() != a2.getStart()) {
					return a1.getStart() - a2.getStart();
				} else {
					return a1.getEnd() - a2.getEnd();
				}
			}
		});

		List<ViewAnnotation> endAnnotations = new ArrayList<>(visibleAnnotations);
		Collections.sort(endAnnotations, new Comparator<ViewAnnotation>() {
			public int compare(ViewAnnotation a1, ViewAnnotation a2) {
				if (a1.getEnd() != a2.getEnd()) {
					return a1.getEnd() - a2.getEnd();
				} else {
					return a1.getStart() - a2.getStart();
				}
			}
		});

		PeekingIterator<ViewAnnotation> beginIt = Iterators.peekingIterator(beginAnnotations.iterator());
		PeekingIterator<ViewAnnotation> endIt = Iterators.peekingIterator(endAnnotations.iterator());

		List<ViewAnnotation> openAnns = new ArrayList<>();
		int lastOffset = 0;

		while (endIt.hasNext()) {
			// Get the position where the next annotation begin/end occurs
			final int nextBegin;
			if (beginIt.hasNext()) {
				nextBegin = beginIt.peek().getStart();
			} else {
				nextBegin = text.length();
			}
			final int nextEnd;
			if (endIt.hasNext()) {
				nextEnd = endIt.peek().getEnd();
			} else {
				nextEnd = text.length();
			}
			int offset = Math.min(nextBegin, nextEnd);

			// Create elements between the last offset and the current offset
			if (lastOffset < offset) {
				createElements(openAnns, lastOffset, offset - 1);
			}

			// Get annotations beginning at this position
			List<ViewAnnotation> beginAnns = new ArrayList<>();
			while (beginIt.hasNext() && beginIt.peek().getStart() == offset) {
				beginAnns.add(beginIt.next());
			}

			// Add the annotations beginning at this position to those already
			// open
			openAnns.addAll(0, beginAnns);

			// Create elements at the current offset
			createElements(openAnns, offset, offset);

			// Remove annotations, from open list, that end at this position
			while (endIt.hasNext() && endIt.peek().getEnd() == offset) {
				openAnns.remove(endIt.next());
			}

			lastOffset = offset + 1;
		}

		if (lastOffset < this.text.length()) {
			// Render any remaining text, without annotations, at the end of the
			// document
			if (!openAnns.isEmpty()) {
				Window.alert("ERROR! openanns should be empty");
			}
			createElements(openAnns, lastOffset, this.text.length());
		}

		updated = false;
		return true;
	}

	private void createElements(List<ViewAnnotation> anns, int begin, int end) {
		int level = 0;
		String spanText = text.substring(begin, end + 1);

		Element childElement;
		if (anns.isEmpty()) {
			addText(spanText, element);
		} else {
			leftMargin = 0;
			rightMargin = 0;

			// Process innermost span (containing span text)
			ViewAnnotation innermostAnn = anns.get(0);
			childElement = createSpanElement(innermostAnn, begin, end, level);
			addText(spanText, childElement);

			// Process the other spans
			for (ViewAnnotation ann : Iterables.skip(anns, 1)) {
				level++;
				SpanElement spanElement = createSpanElement(ann, begin, end, level);
				spanElement.appendChild(childElement);
				childElement = spanElement;
			}
			// Add top most element to main document element
			this.element.appendChild(childElement);
		}

	}

	private int leftMargin;
	private int rightMargin;

	private SpanElement createSpanElement(ViewAnnotation ann, int spanBegin, int spanEnd, int level) {
		if (ann.getLevel() < level) {
			ann.setLevel(level);
		}

		// Insert the annotation level
		SpanElement span = Document.get().createSpanElement();

		span.setAttribute("data-id", "" + ann.getId());

		Style style = span.getStyle();
		span.addClassName(this.style.annotation());
		if (ann.getBackgroundColor() != null) {
			style.setBackgroundColor(ann.getBackgroundColor());
		}
		if (ann.getForegroundColor() != null) {
			style.setColor(ann.getForegroundColor());
		}

		ann.addElement(span);

		
		if (spanBegin != ann.getStart()) {
			leftMargin--;
		} else {
			// This span represents the start of an annotation
			// Setting the margin hides the border(s) of any annotations
			// starting at the character position represented by this span
			span.addClassName(this.style.annotationBegin());
			if (leftMargin < 0) {
			//	style.setMarginLeft(1, Unit.PX);
				leftMargin = 0;
			}
		}
		

		if (spanEnd != ann.getEnd()) {
			rightMargin--;
		} else {
			// This span represents the end of an annotation
			// Setting the margin hides the border(s) of any annotations ending
			// at the character position represented by this span
			span.addClassName(this.style.annotationEnd());
			if (rightMargin < 0) {
				style.setMarginRight(-1, Unit.PX);
				rightMargin = 0;
			}
		}

		return span;
	}

	private void addText(String text, Element elem) {
		int lastIndex = 0;
		do {
			int nextBreakIndex = text.indexOf('\n', lastIndex);

			final String string;
			if (nextBreakIndex >= 0) {
				string = text.substring(lastIndex, nextBreakIndex + 1);
				elem.appendChild(Document.get().createTextNode(string));
				elem.appendChild(Document.get().createBRElement());
			} else {
				string = text.substring(lastIndex);
				elem.appendChild(Document.get().createTextNode(string));
			}

			lastIndex += string.length();

		} while (lastIndex < text.length());
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

	static native float getComputedFontSize(Element element) /*-{
																if ($doc.defaultView && $doc.defaultView.getComputedStyle) {
																var style = $doc.defaultView.getComputedStyle(element, null).getPropertyValue("font-size");
																return parseFloat(style);
																}
																return 0;
																}-*/;

}

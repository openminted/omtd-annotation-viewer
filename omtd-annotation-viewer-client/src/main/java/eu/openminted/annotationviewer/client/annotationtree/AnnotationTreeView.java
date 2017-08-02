package eu.openminted.annotationviewer.client.annotationtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import eu.openminted.annotationviewer.client.style.AnnotationTypeStyle;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;
import eu.openminted.annotationviewer.client.uima.Annotation;
import eu.openminted.annotationviewer.client.uima.Cas;
import gwt.material.design.client.constants.ButtonType;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.constants.Position;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCollapsible;
import gwt.material.design.client.ui.MaterialCollapsibleBody;
import gwt.material.design.client.ui.MaterialCollapsibleHeader;
import gwt.material.design.client.ui.MaterialCollapsibleItem;
import gwt.material.design.client.ui.MaterialDivider;
import gwt.material.design.client.ui.MaterialHeader;
import gwt.material.design.client.ui.MaterialIcon;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.animate.MaterialAnimation;
import gwt.material.design.client.ui.animate.Transition;
import gwt.material.design.client.ui.html.Div;
import gwt.material.design.client.ui.html.Span;

public class AnnotationTreeView extends ViewWithUiHandlers<AnnotationTreeUiHandlers>
		implements AnnotationTreePresenter.MyView, EventListener {
	interface Binder extends UiBinder<Widget, AnnotationTreeView> {
	}

	interface MyStyle extends CssResource {
		String annotationValue();

		String header();

		String body();
	}

	private final static Set<String> featuresToIgnore = Sets.newHashSet("sofa", "begin", "end");
	private static final String EMPTY_STRING = "";
	private static final String NULL_ARRAY_ITEM_STRING = "<null>";

	@UiField
	MyStyle style;

	@UiField
	MaterialCollapsible collapsible;

	private final Map<Integer, MaterialCollapsibleItem> itemMap = new HashMap<>();
	private int currentAnnotationId = -1;

	@Inject
	public AnnotationTreeView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		DOM.setEventListener(collapsible.getElement(), this);
		collapsible.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONCLICK);
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		DOM.setEventListener(collapsible.getElement(), null);
	}

	@Override
	public void clear() {
		itemMap.clear();
		collapsible.clear();
	}

	@Override
	public void addAnnotation(Annotation ann, AnnotationTypeStyles styles) {

		final Div content = createAnnotationContent(ann, styles);

		boolean hasContent = (content != null);
		MaterialCollapsibleHeader header = createHeader(ann, styles, hasContent);

		MaterialCollapsibleItem item = new MaterialCollapsibleItem();
		item.setDataAttribute("id", "" + ann.getId());
		if (ann.isDocumentLevel()) {
			item.setDataAttribute("document-level", "true");
		}
		item.add(header);
		if (content != null) {
			MaterialCollapsibleBody body = new MaterialCollapsibleBody(content);
			body.getElement().addClassName(style.body());
			item.add(body);
		}
		AnnotationTypeStyle typeStyle = styles.getStyle(ann.getTypeName());
		item.setVisible(typeStyle.isVisible());

		collapsible.add(item);
		itemMap.put(ann.getId(), item);
	}

	@Override
	public void showAnnotationValue(Annotation root, List<Annotation> path, AnnotationTypeStyles typeStyles) {

		final Transition transitionOut;
		final Transition transitionIn;

		final MaterialCollapsibleItem item = itemMap.get(root.getId());

		List<Integer> currentPath = parsePathIds(item.getDataAttribute("path"));
		if (currentPath.size() < path.size()) {
			transitionOut = Transition.FADEOUTLEFT;
			transitionIn = Transition.FADEINRIGHT;
		} else {
			transitionOut = Transition.FADEOUTRIGHT;
			transitionIn = Transition.FADEINLEFT;
		}

		final MaterialCollapsibleBody body = item.getBody();
		final MaterialAnimation animOut = new MaterialAnimation();
		animOut.setTransition(transitionOut);
		animOut.setDelay(0);
		animOut.setDuration(150);
		animOut.setInfinite(false);


		final Annotation value;

		final Div newContent = new Div();
		if (path != null && path.size() > 1) {
			StringBuilder sb = new StringBuilder("" + root.getId());
			Iterables.skip(path, 1).forEach(ann -> {
				sb.append('-');
				sb.append(ann.getId());
			});
			item.setDataAttribute("path", sb.toString());

			value = path.get(path.size() - 1);
			AnnotationTypeStyle typeStyle = typeStyles.getStyle(value.getTypeName());

			MaterialIcon backIcon = new MaterialIcon();
			backIcon.setIconType(IconType.CHEVRON_LEFT);
			backIcon.setCircle(true);
			backIcon.setBackgroundColor(Color.WHITE);
			backIcon.setTextColor(Color.BLACK);
			backIcon.setShadow(1);
			backIcon.setMarginRight(20);
			backIcon.setHoverable(true);
			backIcon.setDataAttribute("action", "back");
			
			MaterialLabel snippet = new MaterialLabel(getAnnotationSnippet(value));
			snippet.setShadow(1);
			snippet.setTruncate(true);
			snippet.getElement().addClassName(style.annotationValue());
			Style snippetStyle = snippet.getElement().getStyle();
			snippetStyle.setBackgroundColor(typeStyle.getBackgroundColor());
			snippetStyle.setColor(typeStyle.getForegroundColor());
			snippetStyle.setCursor(Cursor.DEFAULT);
			snippet.setTooltip(value.getTypeName());
			snippet.setTooltipPosition(Position.LEFT);
			
			
			Div header = new Div();
			header.setMarginBottom(15);
			header.getElement().addClassName("valign-wrapper");
			backIcon.getElement().addClassName("valign center");
			snippet.getElement().addClassName("valign center");
			header.add(backIcon);
			header.add(snippet);

			newContent.add(header);
			//newContent.add(new MaterialDivider());
		} else {
			item.getElement().removeAttribute("data-path");
			value = root;
		}

		newContent.add(createAnnotationContent(value, typeStyles));

		Widget currentContent = body.getWidget(0);
		animOut.animate(currentContent, () -> {
			body.clear();
			if (value != null) {
			} else {
				// TODO test
				body.getElement().getStyle().setProperty("paddingTop", null);
			}
			body.add(newContent);

			MaterialAnimation animIn = new MaterialAnimation();
			animIn.setTransition(transitionIn);
			animIn.setDelay(0);
			animIn.setDuration(150);
			animIn.setInfinite(false);
			animIn.animate(newContent);
		});

	}

	@Override
	public void onBrowserEvent(Event event) {
		EventTarget t = event.getEventTarget();
		// https://www.slideshare.net/senchainc/creating-ext-gwt-extensions-and-components

		if (!Element.is(t)) {
			return;
		}
		Element elem = t.cast();

		int id = getAnnotationId(elem);
		switch (event.getTypeInt()) {
		case Event.ONMOUSEOVER:
			if (id != currentAnnotationId) {
				// If moving from one annotation in the list to another, then
				// pass the event to the former annotation.
				if (currentAnnotationId >= 0) {
					onMouseOut(currentAnnotationId);
				}

				onMouseOver(id);
				currentAnnotationId = id;
			}
			break;
		case Event.ONMOUSEOUT:
			if (currentAnnotationId >= 0) {
				// If moving out of the annotation list, inform the last
				// annotation in the list of this event.
				Element mouseElem = elementFromPoint(event.getClientX(), event.getClientY());
				if (mouseElem == null || !collapsible.getElement().isOrHasChild(mouseElem)) {
					onMouseOut(currentAnnotationId);
					currentAnnotationId = 1;
				}
			}
			break;
		case Event.ONCLICK:
			if (elem.hasAttribute("data-ref")) {
				// User has clicked on a nested annotation, so let's slide it
				// into view.
				MaterialCollapsibleItem item = itemMap.get(id);
				String path = item.getDataAttribute("path");
				String ref = elem.getAttribute("data-ref");
				try {
					int refId = Integer.parseInt(ref);
					List<Integer> pathIds = parsePathIds(path);
					if ( pathIds.isEmpty() ) {
						pathIds.add(id);
					}
					pathIds.add(refId);
					getUiHandlers().viewAnnotationValue(id, pathIds);
				} catch (NumberFormatException e) {
					throw e;
				}
			} else if (elem.hasAttribute("data-action")) {
				// User has clicked on the 'back' button when displaying a
				// nested annotation, so let's slide the parent annotation back
				// into view.
				String action = elem.getAttribute("data-action");
				if ("back".equals(action)) {
					MaterialCollapsibleItem item = itemMap.get(id);
					String path = item.getDataAttribute("path");
					try {
						List<Integer> pathIds = parsePathIds(path);
						pathIds.remove(pathIds.size() - 1);
						getUiHandlers().viewAnnotationValue(id, pathIds);
					} catch (NumberFormatException e) {
						throw e;
					}
				}
			}
			break;
		}
	}

	private void onMouseOut(int id) {
		MaterialCollapsibleItem item = itemMap.get(id);

		if (item != null && !"true".equals(item.getDataAttribute("document-level"))) {
			MaterialCollapsibleHeader header = item.getHeader();
			if (header != null) {
				header.remove(0);
			}
		}
	}

	private void onMouseOver(int id) {
		MaterialCollapsibleItem item = itemMap.get(id);
		if (item != null && !"true".equals(item.getDataAttribute("document-level"))) {
			MaterialCollapsibleHeader header = item.getHeader();
			if (header != null) {
				MaterialIcon icon = new MaterialIcon(IconType.VISIBILITY);
				icon.setFloat(Float.RIGHT);
				icon.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						getUiHandlers().viewAnnotationInText(id);
						event.stopPropagation();

					}
				});
				header.insert(icon, 0);
			}
		}
	}

	private List<Integer> parsePathIds(String path) {
		List<Integer> ids = new ArrayList<>();
		if (!Strings.isNullOrEmpty(path)) {
			String[] split = path.split("-");
			for (int i = 0; i < split.length; i++) {
				ids.add(Integer.parseInt(split[i]));
			}
		}
		
		return ids;
	}

	private int getAnnotationId(Element elem) {
		while (elem != null && elem != collapsible.getElement()) {
			if (elem.hasAttribute("data-id")) {
				String idString = elem.getAttribute("data-id");
				try {
					return Integer.parseInt(idString);
				} catch (NumberFormatException e) {
					return -1;
				}
			}
			elem = elem.getParentElement();
		}
		return -1;
	}

	private Div createAnnotationContent(Annotation ann, AnnotationTypeStyles styles) {
		if (ann.isArray()) {
			return createArrayContent(ann, styles);
		} else {
			return createFeatureValuesContent(ann, styles);
		}
	}

	private Div createArrayContent(Annotation ann, AnnotationTypeStyles styles) {
		Div div = null;
		if (ann.arraySize() > 0) {
			div = new Div();
			for (int i = 0; i < ann.arraySize(); i++) {

				if (Cas.TYPE_NAME_FS_ARRAY.equals(ann.getTypeName())) {
					// Annotation array item
					Annotation arrayValue = ann.getArrayValueAsAnnotation(i);
					if (arrayValue == null) {
						div.add(createPrimitiveValueContent("[" + i + ']', NULL_ARRAY_ITEM_STRING));
					} else {
						div.add(createAnnotationValueContent("[" + i + ']', arrayValue, styles));
					}
				} else {
					// Primitive array item
					String arrayValue = ann.getArrayValueAsString(i);
					if (arrayValue == null) {
						arrayValue = NULL_ARRAY_ITEM_STRING;
					}
					div.add(createPrimitiveValueContent("[" + i + ']', arrayValue));
				}
			}
		}
		return div;
	}

	private Div createFeatureValuesContent(Annotation ann, AnnotationTypeStyles styles) {
		Div div = new Div();
		Iterator<String> featureIt = ann.getFeatureNames();
		while (featureIt.hasNext()) {
			String featureName = featureIt.next();

			if (ann.isAnnotation(featureName)) {
				Annotation featureValue = ann.getFeatureValueAsAnnotation(featureName);
				if (featureValue != null) {
					div.add(createAnnotationValueContent(featureName, featureValue, styles));
				}
			} else {
				String featureValue = ann.getFeatureValueAsString(featureName);

				if (isFeatureValid(featureName, featureValue)) {
					div.add(createPrimitiveValueContent(featureName, featureValue));
				}
			}
		}
		if (div.getChildren().size() == 0) {
			div = null;
		}
		return div;
	}

	private MaterialCollapsibleHeader createHeader(Annotation ann, AnnotationTypeStyles styles, boolean hasContent) {
		MaterialCollapsibleHeader header = new MaterialCollapsibleHeader();
		// header.getElement().getStyle().setLineHeight(1.4, Unit.EM);
		MaterialLabel link = new MaterialLabel();
		link.setTruncate(true);
		link.setText(getAnnotationSnippet(ann));
	

		AnnotationTypeStyle typeStyle = styles.getStyle(ann.getTypeName());

		header.getElement().addClassName(style.header());
		Style style = header.getElement().getStyle();
		style.setBackgroundColor(typeStyle.getBackgroundColor());
		style.setColor(typeStyle.getForegroundColor());
		style.setColor(typeStyle.getForegroundColor());
		
		header.setTooltip(ann.getTypeName());
		header.setTooltipPosition(Position.LEFT);
		header.setTooltipDelayMs(50);

		if (hasContent) {
			header.add(new MaterialIcon(IconType.ARROW_DROP_DOWN));
		}
		header.add(link);
		return header;
	}

	private String getAnnotationSnippet(Annotation ann) {
		if (ann.isDocumentLevel()) {
			String type = ann.getTypeName();
			if (type.contains(".")) {
				type = type.substring(type.lastIndexOf('.') + 1);
			}
			return type;
		} else {
			String text = ann.getCoveredText();
			if (text != null) {
				return text;
			} else {
				return EMPTY_STRING;
			}
		}
	}

	@Override
	public void scrollToAnnotation(int id) {
		MaterialCollapsibleItem item = itemMap.get(id);
		if (item != null) {
			// Show features/attributes of the annotation
			item.expand();

			// Scroll to bottom-most visible item in the annotation list. This
			// ensures when
			// scrolling to the selected item that it appears at the top of the
			// page. See:
			// https://stackoverflow.com/questions/39377524/scrolltoview-brings-the-element-at-the-bottom-how-to-scroll-element-to-the-to
			for (int i = collapsible.getWidgetCount() - 1; i >= 0; i--) {
				if (collapsible.getWidget(i).isVisible()) {
					collapsible.getWidget(i).getElement().scrollIntoView();
					break;
				}
			}

			// Scroll to the selected item
			item.getElement().scrollIntoView();

		}
	}

	@Override
	public void setAnnotationVisibility(int id, boolean visible) {
		MaterialCollapsibleItem item = itemMap.get(id);

		if (item != null) {
			item.setVisible(visible);
		}
	}

	@Override
	public void setAnnotationColors(int id, String foregroundColor, String backgroundColor) {
		MaterialCollapsibleItem item = itemMap.get(id);
		if (item != null) {
			Style style = item.getHeader().getElement().getStyle();
			style.setBackgroundColor(backgroundColor);
			style.setColor(foregroundColor);
		}
	}

	private boolean isFeatureValid(String featureName, String featureValue) {
		if (featuresToIgnore.contains(featureName)) {
			return false;
		}
		if (Strings.isNullOrEmpty(featureValue)) {
			return false;
		}
		if (featureValue.startsWith("file:/")) {
			return false;
		}

		return true;
	}

	private Widget createAnnotationValueContent(String name, Annotation ann, AnnotationTypeStyles styles) {

		String value = getAnnotationSnippet(ann);
		if (ann.isDocumentLevel()) {
			value = ann.getTypeName();
			if (value.contains(".")) {
				value = value.substring(value.lastIndexOf('.') + 1);
			}
		} else {
			value = ann.getCoveredText();
		}

		Span nameSpan = new Span(name);
		nameSpan.getElement().getStyle().setFontWeight(FontWeight.BOLD);

		Span valueSpan = new Span(value);
		valueSpan.setHoverable(true);
		valueSpan.setShadow(1);
		Element valueElement = valueSpan.getElement();
		valueElement.addClassName(style.annotationValue());
		valueSpan.setDataAttribute("ref", "" + ann.getId());
		Style valueStyle = valueSpan.getElement().getStyle();
		AnnotationTypeStyle typeStyle = styles.getStyle(ann.getTypeName());
		valueStyle.setBackgroundColor(typeStyle.getBackgroundColor());
		valueStyle.setColor(typeStyle.getForegroundColor());

		Div div = new Div();
		Style divStyle = div.getElement().getStyle();
		divStyle.setProperty("overflowWrap", "break-word");
		divStyle.setProperty("wordWrap", "break-word");
		div.add(nameSpan);
		div.add(valueSpan);
		return div;
	}

	private Widget createPrimitiveValueContent(String name, String value) {
		Div div = new Div();
		Style divStyle = div.getElement().getStyle();
		divStyle.setProperty("overflowWrap", "break-word");
		divStyle.setProperty("wordWrap", "break-word");
		Span nameSpan = new Span(name);
		nameSpan.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		Span valueSpan = new Span(value);
		valueSpan.getElement().getStyle().setPaddingLeft(5, Unit.PX);
		div.add(nameSpan);
		div.add(valueSpan);
		return div;
	}

	private static native Element elementFromPoint(int x, int y) /*-{	
																	return $doc.elementFromPoint(x, y);
																	}-*/;

}

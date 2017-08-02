package eu.openminted.annotationviewer.client.typelist;

import javax.inject.Inject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import gwt.material.design.client.constants.CollectionType;
import gwt.material.design.client.constants.WavesType;
import gwt.material.design.client.ui.MaterialCollection;
import gwt.material.design.client.ui.MaterialCollectionItem;
import gwt.material.design.client.ui.MaterialCollectionSecondary;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialSwitch;

public class TypeListView extends ViewWithUiHandlers<TypeListUiHandlers> implements TypeListPresenter.MyView {
	interface Binder extends UiBinder<HTMLPanel, TypeListView> {
	}

	@UiField
	MaterialCollection collection;

	// @UiField
	// MaterialSearch search;

	private final TypeVisibilityHandler1 typeVisibilityHandler1;
	private final TypeVisibilityHandler2 typeVisibilityHandler2;

	@Inject
	public TypeListView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		this.typeVisibilityHandler1 = new TypeVisibilityHandler1();
		this.typeVisibilityHandler2 = new TypeVisibilityHandler2();
	}

	@Override
	public void clear() {
		collection.clear();
	}

	@Override
	public void addType(String typeName, String backgroundColor, String foregroundColor, boolean visible) {
		MaterialCollectionItem item = new MaterialCollectionItem();
		item.setWaves(WavesType.DEFAULT);
		item.setDataAttribute("id", typeName);
		Style style = item.getElement().getStyle();
		String bg = "linear-gradient(to right," + backgroundColor + ",white)";
		style.setProperty("background", bg);
		style.setColor(foregroundColor);

		MaterialLabel mainLabel;
		MaterialLabel subLabel = null;
		if (typeName.indexOf('.') >= 0) {
			int index = typeName.lastIndexOf(".");
			String main = typeName.substring(index + 1);
			String sub = typeName.substring(0, index);
			mainLabel = new MaterialLabel(main);
			mainLabel.setFontWeight(FontWeight.BOLD);
			subLabel = new MaterialLabel(sub);
			subLabel.setFontSize(0.8, Unit.EM);
		} else {
			mainLabel = new MaterialLabel(typeName);
		}
		MaterialCollectionSecondary secondary = new MaterialCollectionSecondary();
		MaterialSwitch swtch = new MaterialSwitch(visible);
		secondary.add(swtch);

		swtch.addValueChangeHandler(typeVisibilityHandler1);
		item.addClickHandler(typeVisibilityHandler2);

		item.add(secondary);
		if (subLabel != null) {
			item.add(subLabel);
		}
		item.add(mainLabel);

		item.setType(CollectionType.CHECKBOX);

		collection.add(item);

	}

	class TypeVisibilityHandler1 implements ValueChangeHandler<Boolean> {
		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			boolean visible = event.getValue();
			String typeName = null;

			MaterialSwitch swtch = (MaterialSwitch) event.getSource();
			Element elem = swtch.getElement();
			while (elem != collection.getElement() && typeName == null) {
				if (elem.hasAttribute("data-id")) {
					typeName = elem.getAttribute("data-id");
				}
				elem = elem.getParentElement();
			}

			if (typeName != null) {
				getUiHandlers().setTypeVisibility(typeName, visible);
			}
		}
	}

	class TypeVisibilityHandler2 implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			// Get the value of the switch within this item
			MaterialCollectionItem item = (MaterialCollectionItem) event.getSource();
			String typeName = item.getDataAttribute("id");
			if (null == typeName) {
				return;
			}

			for (Widget w : item.getChildren()) {
				if (w instanceof MaterialCollectionSecondary) {
					WidgetCollection c = ((MaterialCollectionSecondary) w).getChildren();
					if (c.size() > 0 && c.get(0) instanceof HasValue) {
						@SuppressWarnings("unchecked")
						HasValue<Boolean> cb = (HasValue<Boolean>) c.get(0);

						// The value of the switch hasn't been updated at this
						// point.
						boolean visible = !cb.getValue();
						getUiHandlers().setTypeVisibility(typeName, visible);
					}
				}

			}

		}
	}
	
	/*
	private void updateTypeItemStyle(Element element, boolean visible) {
		String color;
		if ( visible ) {
			color = "black"
		} else {
			color = "grey"
		}
	} */	
}

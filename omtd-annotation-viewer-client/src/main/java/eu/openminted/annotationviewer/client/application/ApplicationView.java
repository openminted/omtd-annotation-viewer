package eu.openminted.annotationviewer.client.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import eu.openminted.annotationviewer.client.uima.Cas;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.client.base.SearchObject;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.IconPosition;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.events.SearchFinishEvent;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCard;
import gwt.material.design.client.ui.MaterialCardTitle;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialDropDown;
import gwt.material.design.client.ui.MaterialHeader;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialNavBar;
import gwt.material.design.client.ui.MaterialNavBrand;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialSearch;
import gwt.material.design.client.ui.MaterialSearchResult;
import gwt.material.design.client.ui.MaterialTitle;
import gwt.material.design.client.ui.MaterialToast;
import gwt.material.design.client.ui.html.Header;
import gwt.material.design.client.ui.html.Span;

public class ApplicationView extends ViewWithUiHandlers<ApplicationUiHandlers> implements ApplicationPresenter.MyView {
	interface Binder extends UiBinder<Widget, ApplicationView> {
	}

	private static final String ATTRIBUTE_VIEW_ID = "id";
	
	@UiField
	MaterialHeader header;

	@UiField
	SimplePanel annotationTree;

	@UiField
	SimplePanel document;

	@UiField
	MaterialContainer container;

	@UiField
	MaterialModal typesModal;

	@UiField
	MaterialOracleSearch txtSearch;

	@UiField
	MaterialDropDown viewsDropDown;

	@UiField
	MaterialButton viewsButton;

	@UiField
	SimplePanel typeList;

	@UiField
	MaterialNavBar navBar, navBarSearch;
	
	@UiField
	MaterialNavBrand title;
	
	@UiField
	MaterialContainer error;
	
	@UiField
	MaterialLabel errorMessage;

	@Inject
	ApplicationView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		bindSlot(ApplicationPresenter.SLOT_ANNOTATION_TREE, annotationTree);
		bindSlot(ApplicationPresenter.SLOT_DOCUMENT, document);
		bindSlot(ApplicationPresenter.SLOT_TYPE_LIST, typeList);

		txtSearch.addOpenHandler(openEvent -> {
			navBar.setVisible(false);
			navBarSearch.setVisible(true);
			container.getElement().getStyle().setPaddingTop(0, Unit.PX);
		});
		txtSearch.addCloseHandler(event -> {
			navBar.setVisible(true);
			navBarSearch.setVisible(false);
			container.getElement().getStyle().setPaddingTop(64, Unit.PX);
			// MaterialToast.fireToast("Close Event was fired");
		});
	
		txtSearch.addValueChangeHandler(event -> {
			getUiHandlers().onSearchAutoSuggest(event.getValue());
		}); 
		
		title.getElement().addClassName("truncate");

	}

	@Override
	public void showApplication() {
		header.setVisible(true);
		container.setVisible(true);
	}
	
	@Override
	public void showError(String message) {
		/*
		MaterialCardTitle title = new MaterialCardTitle();
		title.setText("Houston, we have a problem...");
		title.setIconType(IconType.ERROR);
		title.setIconPosition(IconPosition.LEFT);
		
		MaterialCard card = new MaterialCard();
		card.add(title);
		card.add(new MaterialLabel(message));
		error.add(card); */
		errorMessage.setText(message);
		error.setVisible(true);
		
	}

	@Override
	public void setViewNames(Set<String> viewNames) {
		viewsDropDown.clear();
		if (viewNames.size() > 1) {
			for (String viewName : viewNames) {
				MaterialLabel label = new MaterialLabel();
				label.setDataAttribute(ATTRIBUTE_VIEW_ID, viewName);
				if (Cas.NAME_DEFAULT_SOFA.equals(viewName) && !viewNames.contains("Default")) {
					label.setText("Default");
				} else {
					label.setText(viewName);
				}

				label.setTextColor(Color.BLACK);
				viewsDropDown.add(label);
			}
			viewsButton.setVisible(true);
		} else {
			viewsButton.setVisible(false);
		}
	}

	@Override
	public void setView(String viewName) {
		for (Widget w : viewsDropDown.getItems()) {
			if (w instanceof MaterialLabel) {
				MaterialLabel label = (MaterialLabel) w;
				if (viewName.equals(label.getDataAttribute(ATTRIBUTE_VIEW_ID))) {
					label.setFontWeight(FontWeight.BOLD);
				} else {
					label.setFontWeight(FontWeight.NORMAL);
				}
			}
		}
	}
	
	@Override
	public void setTitle(String title) {
		//this.title.setText(title);
		this.title.clear();
		Span span = new Span(title);
		span.setTruncate(true);
		this.title.add(span);
		Window.setTitle(title);
	}


	@UiHandler("searchButton")
	void onSearch(ClickEvent e) {
		txtSearch.open();
	}

	@UiHandler("viewsDropDown")
	void onViewsDropDown(SelectionEvent<Widget> event) {
		MaterialLabel label = (MaterialLabel) event.getSelectedItem();
		getUiHandlers().onViewChange(label.getDataAttribute(ATTRIBUTE_VIEW_ID));
	}


	@UiHandler("openTypesButton")
	void onOpenTypesModal(ClickEvent event) {
		typesModal.open();
	}

	@UiHandler("closeTypesButton")
	void onCloseTypesModal(ClickEvent event) {
		typesModal.close();
	}

	@Override
	public void setOracle(SuggestOracle oracle) {
		// TODO Auto-generated method stub
		
	}

}

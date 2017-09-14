package eu.openminted.annotationviewer.client.application;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

//import com.google.common.collect.Sets;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import eu.openminted.annotationviewer.client.NameTokens;
import eu.openminted.annotationviewer.client.annotationtree.AnnotationTreePresenter;
import eu.openminted.annotationviewer.client.annotationtree.event.ViewAnnotationInTextEvent;
import eu.openminted.annotationviewer.client.annotationtree.event.ViewAnnotationInTextEvent.ViewAnnotationInTextHandler;
import eu.openminted.annotationviewer.client.service.AnnotationRestApi;
import eu.openminted.annotationviewer.client.service.dto.Document;
import eu.openminted.annotationviewer.client.service.impl.AnnotationRestApiImpl;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;
import eu.openminted.annotationviewer.client.style.impl.AnnotationTypeStylesImpl;
import eu.openminted.annotationviewer.client.typelist.TypeListPresenter;
import eu.openminted.annotationviewer.client.typelist.event.SetTypeVisibilityEvent;
import eu.openminted.annotationviewer.client.typelist.event.SetTypeVisibilityEvent.SetTypeVisibilityHandler;
import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.uima.CasUtils;
import eu.openminted.annotationviewer.client.viewer.DocumentPresenter;
import eu.openminted.annotationviewer.client.viewer.event.AnnotationsSelectedEvent;
import eu.openminted.annotationviewer.client.viewer.event.AnnotationsSelectedEvent.AnnotationsSelectedHandler;
import gwt.material.design.client.ui.MaterialLoader;

public class ApplicationPresenter extends Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy>
		implements ApplicationUiHandlers, AnnotationsSelectedHandler, ViewAnnotationInTextHandler,
		SetTypeVisibilityHandler {
	interface MyView extends View, HasUiHandlers<ApplicationUiHandlers> {
		void setViewNames(Set<String> viewNames);

		void setTitle(String title);

		void setView(String viewName);

		void setOracle(SuggestOracle oracle);

		void showApplication();

		void showError(String message);
	}

	@ProxyStandard
	@NameToken(NameTokens.HOME)
	interface MyProxy extends ProxyPlace<ApplicationPresenter> {
	}

	// private final static Set<String> featuresToIgnore = Sets.newHashSet("sofa",
	// "begins", "ends");

	static final PermanentSlot<AnnotationTreePresenter> SLOT_ANNOTATION_TREE = new PermanentSlot<>();
	static final PermanentSlot<DocumentPresenter> SLOT_DOCUMENT = new PermanentSlot<>();
	static final PermanentSlot<TypeListPresenter> SLOT_TYPE_LIST = new PermanentSlot<>();

	private final AnnotationTreePresenter annotationTree;
	private final DocumentPresenter document;
	private final TypeListPresenter typeList;

	private final AnnotationTypeStyles typeStyles = new AnnotationTypeStylesImpl();

	private String documentId;

	private String initialViewId;
	private Cas viewCas;
	private Cas cas;

	@Inject
	ApplicationPresenter(EventBus eventBus, MyView view, MyProxy proxy, AnnotationTreePresenter annotationTree,
			DocumentPresenter document, TypeListPresenter typeList) {
		super(eventBus, view, proxy, RevealType.Root);
		this.annotationTree = annotationTree;
		this.document = document;
		this.typeList = typeList;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		setInSlot(SLOT_ANNOTATION_TREE, annotationTree);
		setInSlot(SLOT_DOCUMENT, document);
		setInSlot(SLOT_TYPE_LIST, typeList);

		addRegisteredHandler(AnnotationsSelectedEvent.getType(), this);
		addRegisteredHandler(ViewAnnotationInTextEvent.getType(), this);
		addRegisteredHandler(SetTypeVisibilityEvent.getType(), this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);

		String archiveId = request.getParameter("archiveId", "");
		this.documentId = request.getParameter("documentId", "");
		initialViewId = request.getParameter("viewId", null);

		retrieveDocument(archiveId, documentId);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		MaterialLoader.showLoading(true);
	}

	private void retrieveDocument(String archiveId, String documentId) {
		AnnotationRestApi annotationService = new AnnotationRestApiImpl("http://localhost:8080/api/");
		annotationService.getDocument(archiveId, documentId).subscribe(this::onGetDocumentRetrieved,
				this::onGetDocumentError);
	}

	private void onGetDocumentError(Throwable error) {
		getView().showError("We were unable to retrieve the requested document.");
		MaterialLoader.showLoading(false);
	}

	private void onGetDocumentRetrieved(Document document) {
		this.cas = document.getCas();

		Iterator<Cas> viewIt = cas.getViewIterator();
		Set<String> viewNames = new HashSet<>();
		while (viewIt.hasNext()) {
			viewNames.add(viewIt.next().getViewName());
		}
		getView().setViewNames(viewNames);

		// Show the default CAS
		String viewId = cas.getViewName();
		if (initialViewId != null) {
			Cas initialCas = cas.getView(initialViewId);
			if (initialCas != null) {
				viewId = initialViewId;
			}
		}
		setView(viewId);
		MaterialLoader.showLoading(false);
		getView().showApplication();
	}

	@Override
	public void onAnnotationsSelected(AnnotationsSelectedEvent event) {
		int id = event.getIds().get(0);
		annotationTree.scrollAnnotationIntoView(id);
		scrollFix();
	}

	@Override
	public void onViewAnnotationInText(ViewAnnotationInTextEvent event) {
		document.scrollAnnotationIntoView(event.getId());
		scrollFix();
	}

	@Override
	public void onSetTypeVisibility(SetTypeVisibilityEvent event) {
		String typeName = event.getTypeName();
		boolean visible = event.isVisible();
		typeStyles.getStyle(typeName).setVisible(visible);

		document.setAnnotationTypeVisibility(typeName, visible);
		annotationTree.setAnnotationTypeVisibility(typeName, visible);
	}

	@Override
	public void onViewChange(String viewId) {
		setView(viewId);
	}

	MultiWordSuggestOracle oracle;
	SuggestOracle.Request pendingRequest;

	@Override
	public void onSearchAutoSuggest(String query) {
		pendingRequest = new SuggestOracle.Request(query);

		// Todo only do this on a delay
		oracle.requestSuggestions(pendingRequest, (req, res) -> {
			Window.alert("" + (res.getSuggestions().size() + res.getMoreSuggestionsCount()));
		});
	}

	private void setView(String viewId) {
		if (viewCas == null || !viewCas.getViewName().equals(viewId)) {
			Cas newCas = cas.getView(viewId);
			if (newCas != null) {

				String title = CasUtils.getTitle(newCas);
				if (title == null) {
					title = documentId;
				}
				getView().setTitle(title);

				this.document.setCas(newCas, typeStyles);
				this.annotationTree.setCas(newCas, typeStyles);
				typeList.setCas(newCas, typeStyles);
				getView().setView(viewId);
				viewCas = newCas;

				/*
				 * oracle = new MultiWordSuggestOracle("|"); Iterator<Annotation> it =
				 * viewCas.getAllAnnotations(); while (it.hasNext()) { Annotation ann =
				 * it.next(); Iterator<String> featureIt = ann.getFeatureNames(); while
				 * (featureIt.hasNext()) { String feature = featureIt.next(); if
				 * (featuresToIgnore.contains(feature)) { continue; } String value =
				 * ann.getFeatureValueAsString(feature); if (value != null) { oracle.add(feature
				 * + ':' + value); } }
				 * 
				 * }
				 */
			}
		}
	}

	/**
	 * This is required to stop the selected annotation list item being obscured by
	 * the fixed header (as it would otherwise be positioned right at the top page,
	 * directly under the header).
	 */
	private void scrollFix() {
		Window.scrollTo(Window.getScrollTop(), Window.getScrollTop() - 64);
	}

}
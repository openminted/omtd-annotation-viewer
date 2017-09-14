package eu.openminted.annotationviewer.client.application;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;

import gwt.material.design.client.base.HasActive;
import gwt.material.design.client.base.HasSearchHandlers;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.constants.InputType;
import gwt.material.design.client.events.SearchFinishEvent.SearchFinishHandler;
import gwt.material.design.client.events.SearchNoResultEvent.SearchNoResultHandler;
import gwt.material.design.client.ui.MaterialIcon;
import gwt.material.design.client.ui.MaterialSearchResult;
import gwt.material.design.client.ui.MaterialValueBox;
import gwt.material.design.client.ui.html.Label;

import static gwt.material.design.jquery.client.api.JQuery.$;

/**
 * Modified version GWT Material's MaterialSearch, but uses a GWT SuggestOracle to
 * provide suggestions rather than a fixed list of objects.
 * 
 * @see gwt.material.design.client.ui.MaterialSearch
 *
 */
public class MaterialOracleSearch extends MaterialValueBox<String>
		implements HasOpenHandlers<String>, HasCloseHandlers<String>, HasActive, HasSearchHandlers {

	private final Label label = new Label();
	private final MaterialIcon iconSearch = new MaterialIcon(IconType.SEARCH);
	private final MaterialIcon iconClose = new MaterialIcon(IconType.CLOSE);

	private MaterialSearchResult searchResultPanel;
	
	private boolean active;

	private SuggestOracle oracle;
	
	 public MaterialOracleSearch() {
	        super(new TextBox());
	    }
	
	public void setSuggestOracle(SuggestOracle oracle) {
		this.oracle = oracle;
		// clear?
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		build();
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		clear();
		// setCurSel(-1);
	}

	@Override
	protected void build() {
		setType(InputType.SEARCH);
		label.add(iconSearch);
		label.getElement().setAttribute("for", "search");
		add(label);
		add(iconClose);
		iconClose.addMouseDownHandler(mouseDownEvent ->
		CloseEvent.fire(MaterialOracleSearch.this, getText()));

		if (searchResultPanel == null || !searchResultPanel.isAttached()) {
			searchResultPanel = new MaterialSearchResult();
			add(searchResultPanel);
		}
	}

	@Override
	protected void initialize() {
	        addKeyUpHandler(new KeyUpHandler() {
	            @Override
	            public void onKeyUp(KeyUpEvent event) {
	                String keyword = getText().toLowerCase();
	                searchResultPanel.clear();
	                //tempSearches.clear();

	                // Get suggestions
	                
	                /*
	                // Apply selected search
	                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && !tempSearches.isEmpty()) {
	                    if (getCurSel() == -1) {
	                        setSelectedObject(tempSearches.get(0));
	                        setSelectedLink((MaterialLink) searchResultPanel.getWidget(0));
	                    } else {
	                        setSelectedObject(tempSearches.get(curSel));
	                    }

	                    MaterialLink selLink = getSelectedLink();
	                    if (!selLink.getHref().isEmpty()) {
	                        locateSearch(selLink.getHref());
	                    }
	                    reset(selLink.getText());
	                }

	                // Fire an event if there's no search result
	                if (searchResultPanel.getWidgetCount() == 0) {
	                    SearchNoResultEvent.fire(MaterialSearch.this);
	                }

	                // Selection logic using key down event to navigate the search results
	                int totalItems = searchResultPanel.getWidgetCount();
	                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
	                    if (curSel >= totalItems) {
	                        setCurSel(getCurSel());
	                        applyHighlightedItem((MaterialLink) searchResultPanel.getWidget(curSel - 1));
	                    } else {
	                        setCurSel(getCurSel() + 1);
	                        applyHighlightedItem((MaterialLink) searchResultPanel.getWidget(curSel));
	                    }
	                }

	                // Selection logic using key up event to navigate the search results
	                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
	                    if (curSel <= -1) {
	                        setCurSel(-1);
	                        applyHighlightedItem((MaterialLink) searchResultPanel.getWidget(curSel));
	                    } else {
	                        setCurSel(getCurSel() - 1);
	                        applyHighlightedItem((MaterialLink) searchResultPanel.getWidget(curSel));
	                    }
	                }
	                */
	            }
			
			// Resets the search result panel
            private void reset(String keyword) {
                //SearchFinishEvent.fire(MaterialSearch.this);
               // curSel = -1;
                setText(keyword);
                $(valueBoxBase.getElement()).focus();
                searchResultPanel.clear();
            }
		});
	}
	
	@Override
    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setTextColor(Color.BLACK);
            iconClose.setIconColor(Color.BLACK);
            iconSearch.setIconColor(Color.BLACK);
        } else {
            iconClose.setIconColor(Color.WHITE);
            iconSearch.setIconColor(Color.WHITE);
        }
    }

	/**
	 * Programmatically open the search input field component
	 */
	public void open() {
		setActive(true);
		Scheduler.get().scheduleDeferred(() -> $(valueBoxBase.getElement()).focus());
		OpenEvent.fire(MaterialOracleSearch.this, getText());
	}

	@Override
	public HandlerRegistration addSearchFinishHandler(SearchFinishHandler handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HandlerRegistration addSearchNoResultHandler(SearchNoResultHandler handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HandlerRegistration addCloseHandler(final CloseHandler<String> handler) {
		return addHandler((CloseHandler<String>) handler::onClose, CloseEvent.getType());
	}

	@Override
	public HandlerRegistration addOpenHandler(OpenHandler<String> handler) {
		return addHandler((OpenHandler<String>) handler::onOpen, OpenEvent.getType());
	}

}

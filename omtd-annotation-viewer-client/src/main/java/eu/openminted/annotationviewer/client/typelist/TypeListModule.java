package eu.openminted.annotationviewer.client.typelist;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;


public class TypeListModule  extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenterWidget(TypeListPresenter.class, TypeListPresenter.MyView.class,
				TypeListView.class);
	}
}


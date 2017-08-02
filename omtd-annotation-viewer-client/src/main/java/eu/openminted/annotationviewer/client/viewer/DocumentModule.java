package eu.openminted.annotationviewer.client.viewer;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;


public class DocumentModule extends AbstractPresenterModule {

		@Override
		protected void configure() {
			bindPresenterWidget(DocumentPresenter.class, DocumentPresenter.MyView.class,
					DocumentView.class);
		}
}

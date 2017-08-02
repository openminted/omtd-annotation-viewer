package eu.openminted.annotationviewer.client.annotationtree;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class AnnotationTreeModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenterWidget(AnnotationTreePresenter.class, AnnotationTreePresenter.MyView.class,
				AnnotationTreeView.class);
	}
}
